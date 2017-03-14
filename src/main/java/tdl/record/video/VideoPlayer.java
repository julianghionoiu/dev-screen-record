package tdl.record.video;

import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import tdl.record.image.output.ImageOutput;
import tdl.record.time.SystemTimeSource;
import tdl.record.time.TimeSource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class VideoPlayer {
    private final ImageOutput imageOutput;
    private final TimeSource timeSource;
    private Demuxer demuxer;
    private DemuxerStream videoStream;
    private MediaPicture picture;
    private MediaPictureConverter converter;
    private Rational streamTimebase;
    private Rational systemTimeBase;
    private long previousStreamEndTime;

    public VideoPlayer(ImageOutput imageOutput) {
        this(imageOutput, new SystemTimeSource());
    }

    public VideoPlayer(ImageOutput imageOutput, TimeSource timeSource) {
        this.imageOutput = imageOutput;
        this.timeSource = timeSource;
    }

    public void open(String filename) throws IOException, InterruptedException {
        imageOutput.open();

        // A demuxer can separate the media streams from a media file ( video, audio, subtitles )
        demuxer = Demuxer.make();
        demuxer.open(filename, null, false, true, null, null);

        videoStream = getFirstVideoStreamFrom(demuxer)
                .orElseThrow(() -> new RuntimeException("Could not find video stream in container " + filename));


        int videoStreamId = videoStream.getIndex();
        System.out.println("videoStreamId: " + videoStreamId);
        System.out.println("stream.getIndex(): " + videoStream.getIndex());
        Decoder videoDecoder = videoStream.getDecoder();

        /*
         * Now we have found the video stream in this file.  Let's open up our decoder so it can
         * do work.
         */
        videoDecoder.open(null, null);

        /*
          Care must be taken so that the picture is decoded and converted into a meaningful format for the output
         */
        picture = MediaPicture.make(
                videoDecoder.getWidth(),
                videoDecoder.getHeight(),
                videoDecoder.getPixelFormat());
        BufferedImage outputSample = imageOutput
                .getSuggestedOutputSample(videoDecoder.getWidth(), videoDecoder.getHeight());
        converter = MediaPictureConverterFactory.createConverter(
                outputSample,
                picture);


        /*
           With videos it is all about timing.
           We need to make sure we represent the two time frames:
            - The input reference (video)
            - The output reference, if you are rendering to the screen, that is the real time

           If you play with the references, it is possible to speed up or slow down the passing of time
         */
        streamTimebase = videoStream.getTimeBase();
        System.out.println("streamTimebase = " + streamTimebase);
        long streamVideoDuration = videoStream.getDuration();
        System.out.println("streamVideoDuration: " + streamVideoDuration);

        // Set units for the system time, which will be in nanoseconds.
        systemTimeBase = Rational.make(1, 1000000000);
        System.out.println("systemTimeBase = " + systemTimeBase);
        long systemVideoDuration = systemTimeBase.rescale(streamVideoDuration, streamTimebase);
        System.out.println("systemVideoDuration: " + systemVideoDuration);

        // Reset time counters
        previousStreamEndTime = -1;
    }

    public Duration getDuration() {
        Rational systemTimeBase = Rational.make(1);
        long durationInSec = systemTimeBase
                .rescale(videoStream.getDuration(), videoStream.getTimeBase());
        return Duration.of(durationInSec, ChronoUnit.SECONDS);
    }

    public double getFrameRate() {
        return videoStream.getFrameRate().getValue();
    }

    public int getWidth() {
        return picture.getWidth();
    }

    public int getHeight() {
        return picture.getHeight();
    }

    //TODO Fix the duplication between this method and play
    public void seekTo(long timestamp, TimeUnit timeUnit) throws IOException, InterruptedException {
        BufferedImage image = null;

        long targetStreamTimestamp = streamTimebase.rescale(timeUnit.toNanos(timestamp), systemTimeBase);

        /*
          One important thing to bare in mind is that the objects are being reused for performance reasons.
          This packet and the picture will be reset whenever we have new data.
         */
        final MediaPacket packet = MediaPacket.make();
        boolean foundKeyFrame = false;
        while (!foundKeyFrame && demuxer.read(packet) >= 0) {
            // Check if the packet belongs to the video stream
            if (packet.getStreamIndex() == videoStream.getIndex()) {
                int offset = 0;
                int bytesRead = 0;

                // Consume all the frames in the current packet
                do {
                    bytesRead += videoStream.getDecoder().decode(picture, packet, offset);
                    if (picture.isComplete()) {
                        if (picture.getTimeStamp() > targetStreamTimestamp) {
                            image = converter.toImage(image, picture);
                            imageOutput.writeImage(image);

                            previousStreamEndTime = picture.getTimeStamp();

                            foundKeyFrame = true;
                        }
                    }
                    offset += bytesRead;
                } while (offset < packet.getSize());
            }
        }
    }

    public void play() throws InterruptedException, IOException {

        BufferedImage image = null;

        // Calculate the time BEFORE we start playing.
        long streamStartTime;
        if (previousStreamEndTime > -1) {
            streamStartTime = previousStreamEndTime;
        } else {
            streamStartTime = videoStream.getStartTime();
        }

        long systemStartTime = timeSource.currentTimeNano();


        /*
          One important thing to bare in mind is that the objects are being reused for performance reasons.
          This packet and the picture will be reset whenever we have new data.
         */
        final MediaPacket packet = MediaPacket.make();
        while (demuxer.read(packet) >= 0) {
            // Check if the packet belongs to the video stream
            if (packet.getStreamIndex() == videoStream.getIndex()) {
                int offset = 0;
                int bytesRead = 0;

                // Consume all the frames in the current packet
                do {
                    bytesRead += videoStream.getDecoder().decode(picture, packet, offset);
                    if (picture.isComplete()) {
                        image = displayVideoAtCorrectTime(streamStartTime, picture,
                                converter, image, imageOutput, systemStartTime, systemTimeBase,
                                streamTimebase);
                        previousStreamEndTime = picture.getTimeStamp();
                    }
                    offset += bytesRead;
                } while (offset < packet.getSize());
            }
        }

        /*
          Flush the encoder by reading data until we get a new (incomplete) picture
         */
        do {
            videoStream.getDecoder().decode(picture, null, 0);
            if (picture.isComplete()) {
                image = displayVideoAtCorrectTime(streamStartTime, picture, converter,
                        image, imageOutput, systemStartTime, systemTimeBase, streamTimebase);
                previousStreamEndTime = picture.getTimeStamp();
            }
        } while (picture.isComplete());
    }

    public void close() throws IOException, InterruptedException {
        imageOutput.close();
        demuxer.close();
    }

    private BufferedImage displayVideoAtCorrectTime(long streamStartTime,
                                                    final MediaPicture picture, final MediaPictureConverter converter,
                                                    BufferedImage image, ImageOutput imageOutput, long systemStartTime,
                                                    final Rational systemTimeBase, final Rational streamTimebase)
            throws InterruptedException {
        long streamTimestamp = picture.getTimeStamp();
        System.out.println("streamStartTime: " + Long.toString(streamStartTime));
        System.out.println("streamTimestamp: " + Long.toString(streamTimestamp));

        // convert streamTimestamp into system units (i.e. nano-seconds)
        System.out.println("systemTimeBase: " + systemTimeBase);
        long relativeStreamTimestamp = systemTimeBase.rescale(streamTimestamp - streamStartTime, streamTimebase);
        System.out.println("relativeStreamTimestamp: " + Long.toString(relativeStreamTimestamp));
        System.out.println("systemStartTime: " + systemStartTime);
        long targetSystemTimestamp = systemStartTime + relativeStreamTimestamp;
        System.out.println("targetSystemTimestamp: " + Long.toString(targetSystemTimestamp));
        timeSource.wakeUpAt(targetSystemTimestamp, TimeUnit.NANOSECONDS);

        // Convert the image from Humble format into Java images.
        image = converter.toImage(image, picture);
        imageOutput.writeImage(image);
        return image;
    }

    private static Optional<DemuxerStream> getFirstVideoStreamFrom(Demuxer demuxer) throws InterruptedException, IOException {
        int numStreams = demuxer.getNumStreams();
        for (int i = 0; i < numStreams; i++) {
            DemuxerStream currentStream = demuxer.getStream(i);
            final Decoder decoder = currentStream.getDecoder();
            if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
                return Optional.of(currentStream);
            }
        }

        return Optional.empty();
    }
}
