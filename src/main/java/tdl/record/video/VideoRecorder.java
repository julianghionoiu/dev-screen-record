package tdl.record.video;

import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import lombok.extern.slf4j.Slf4j;
import tdl.record.image.input.ImageInput;
import tdl.record.image.input.InputImageGenerationException;
import tdl.record.metrics.RecorderMetricsCollector;
import tdl.record.time.SystemTimeSource;
import tdl.record.time.TimeSource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class VideoRecorder {
    private final ImageInput imageInput;
    private final TimeSource timeSource;
    private final RecorderMetricsCollector metrics;
    private Muxer muxer;
    private Encoder encoder;
    private Rational videoFrameRate;
    private Rational inputFrameRate;
    private MediaPacket packet;
    private final AtomicBoolean shouldStopJob = new AtomicBoolean(false);

    private VideoRecorder(ImageInput imageInput, TimeSource timeSource,
                          RecorderMetricsCollector recorderMetricsCollector) {
        this.imageInput = imageInput;
        this.timeSource = timeSource;
        this.metrics = recorderMetricsCollector;
    }

    public static class Builder {
        private final ImageInput bImageInput;
        private TimeSource bTimeSource;
        private RecorderMetricsCollector bMetrics;

        public Builder(ImageInput imageInput) {
            bImageInput = imageInput;
            bTimeSource = new SystemTimeSource();
            bMetrics = new RecorderMetricsCollector();
        }

        public Builder withTimeSource(TimeSource timeSource) {
            this.bTimeSource = timeSource;
            return this;
        }

        public Builder withMetricsCollector(RecorderMetricsCollector metricsCollector) {
            this.bMetrics = metricsCollector;
            return this;
        }

        public VideoRecorder build() {
            return new VideoRecorder(bImageInput, bTimeSource, bMetrics);
        }
    }

    public void open(String filename, int snapsPerSecond, int timeSpeedUpFactor) throws VideoRecorderException {
        /*
           With videos it is all about timing.
           We need to keep two frames of reference, one for the input and one for the output (video)
           This way, we can produce videos that play at a different speed from the actual input.
         */
        inputFrameRate = Rational.make(1, snapsPerSecond);
        videoFrameRate = Rational.make(1, timeSpeedUpFactor * snapsPerSecond);

        // Prime the image input
        log.info("Open the input stream");
        try {
            imageInput.open();
        } catch (InputImageGenerationException e) {
            throw new VideoRecorderException("Could not open input source", e);
        }

        // A muxer is responsible for combining multiple streams (video, audio, subtitle)
        muxer = Muxer.make(filename, null, null);

        // We are using the default format, which right on OSX defaults to CODEC_ID_H264
        // WARNING! The coded selection is system dependent and will use JNI to retrieve the local codec
        final MuxerFormat format = muxer.getFormat();
        final Codec codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());

        // An encoder is responsible for putting together all the frames from one stream
        encoder = Encoder.make(codec);
        encoder.setWidth(imageInput.getWidth());
        encoder.setHeight(imageInput.getHeight());
        encoder.setPixelFormat(PixelFormat.Type.PIX_FMT_YUV420P);
        encoder.setTimeBase(videoFrameRate);

        // For extra safety, some formats need global rather than per-stream headers
        if (muxer.getFormat().getFlag(MuxerFormat.Flag.GLOBAL_HEADER)) {
            encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
        }

        // Open the stream and the muxer
        encoder.open(null, null);
        muxer.addNewStream(encoder);
        try {
            muxer.open(null, null);
        } catch (InterruptedException | IOException e) {
            throw new VideoRecorderException("Failed to open destination", e);
        }

        // Create the packet to be re-used for encoding
        packet = MediaPacket.make();
    }

    public void start(Duration duration) throws VideoRecorderException {
        try {
            doRecord(duration);
        } catch (RuntimeException e) {
            throw new VideoRecorderException("Fatal exception while recording", e);
        } finally {
            flush();
        }
    }

    private void doRecord(Duration duration) throws VideoRecorderException {
    /*
      Care must be taken so that the picture is encoded using the same format as Encoder.
      The images must be converted so that the match.
     */
        final MediaPicture picture = MediaPicture.make(
                encoder.getWidth(),
                encoder.getHeight(),
                encoder.getPixelFormat());
        picture.setTimeBase(videoFrameRate);
        BufferedImage sampleImage;
        try {
            sampleImage = imageInput.getSampleImage();
        } catch (InputImageGenerationException e) {
            throw new VideoRecorderException("Could not get sample image from input source", e);
        }
        MediaPictureConverter converter = MediaPictureConverterFactory
                .createConverter(sampleImage, picture);

        /*
          One important thing to bare in mind is that the objects are being reused for performance reasons.
          This packet and the picture will be reset whenever we have new data.
         */
        double totalNumberOfFrames = inputFrameRate.rescale(duration.getSeconds(), Rational.make(1));
        double timeBetweenFramesMillis = inputFrameRate.getValue() * 1000;
        metrics.setExpectedTimeBetweenFrames(timeBetweenFramesMillis, TimeUnit.MILLISECONDS);
        for (long frameIndex = 0; frameIndex < totalNumberOfFrames; frameIndex++) {
            long timestampBeforeProcessing = timeSource.currentTimeNano();
            metrics.notifyFrameStartAt(timestampBeforeProcessing, TimeUnit.NANOSECONDS, frameIndex);

            final BufferedImage screen;
            try {
                screen = imageInput.readImage();
            } catch (InputImageGenerationException e) {
                log.error("Failed to acquire image", e);
                break;
            }
            converter.toPicture(picture, screen, frameIndex);

            // Flush the packet, the convention is to write until we get a new (incomplete) packet
            do {
                encoder.encode(packet, picture);
                if (packet.isComplete())
                    muxer.write(packet, false);
            } while (packet.isComplete());


            /*
              With recordings, the biggest challenge is to maintain the requested frameRate.
              We need to trigger the read the next image at exactly the right time.
             */
            metrics.notifyFrameEndAt(timeSource.currentTimeNano(), TimeUnit.NANOSECONDS, frameIndex);
            long nextTimestamp = timestampBeforeProcessing + TimeUnit.MILLISECONDS.toNanos((long) timeBetweenFramesMillis);
            try {
                timeSource.wakeUpAt(nextTimestamp, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                log.debug("Interrupted while sleeping", e);
            }

            // Allow a different thread to stop the recording
            if (shouldStopJob.get()) {
                break;
            }
        }
    }

    public void stop() {
        log.info("Stopping recording");
        shouldStopJob.set(true);
    }


    public void close() {
        log.info("Closing the video stream");
        imageInput.close();
        muxer.close();
    }

    /*
      Flush the encoder by writing data until we get a new (incomplete) packet
     */
    private void flush() {
        log.info("Flushing remaining frames");
        do {
            encoder.encode(packet, null);
            if (packet.isComplete())
                muxer.write(packet, false);
        } while (packet.isComplete());
    }
}
