package record.video;

import io.humble.video.*;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import record.image.input.ImageInput;
import record.image.input.InputImageGenerationException;
import record.time.SystemTimeSource;
import record.time.TimeSource;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class VideoRecorder {
    private final ImageInput imageInput;
    private final TimeSource timeSource;
    private Muxer muxer;
    private Encoder encoder;
    private Rational videoFrameRate;
    private Rational inputFrameRate;

    public VideoRecorder(ImageInput imageInput) {
        this(imageInput, new SystemTimeSource());
    }

    public VideoRecorder(ImageInput imageInput, TimeSource timeSource) {
        this.imageInput = imageInput;
        this.timeSource = timeSource;
    }

    //TODO Sort out exceptions
    public void open(String filename, int snapsPerSecond, int timeSpeedUpFactor)
            throws AWTException, IOException, InterruptedException, InputImageGenerationException {
        /*
           With videos it is all about timing.
           We need to keep two frames of reference, one for the input and one for the output (video)
           This way, we can produce videos that play at a different speed from the actual input.
         */
        inputFrameRate = Rational.make(1, snapsPerSecond);
        videoFrameRate = Rational.make(1, timeSpeedUpFactor * snapsPerSecond);

        // Prime the image input
        imageInput.open();

        // A muxer is responsible for combining multiple streams (video, audio, subtitle)
        muxer = Muxer.make(filename, null, null);

        // We are using the default format, which right now defaults to CODEC_ID_H264
        //TODO Read about CODEC_ID_H264 https://sidbala.com/h-264-is-magic/
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
        muxer.open(null, null);
    }

    public void record(Duration duration)
            throws AWTException, InterruptedException, IOException, InputImageGenerationException {

        /*
          Care must be taken so that the picture is encoded using the same format as Encoder.
          The images must be converted so that the match.
         */
        final MediaPicture picture = MediaPicture.make(
                encoder.getWidth(),
                encoder.getHeight(),
                encoder.getPixelFormat());
        picture.setTimeBase(videoFrameRate);
        MediaPictureConverter converter = MediaPictureConverterFactory
                .createConverter(imageInput.getSampleImage(), picture);


        /*
          One important thing to bare in mind is that the objects are being reused for performance reasons.
          This packet and the picture will be reset whenever we have new data.
         */
        final MediaPacket packet = MediaPacket.make();
        double totalNumberOfFrames = inputFrameRate.rescale(duration.getSeconds(), Rational.make(1));
        for (long frameIndex = 0; frameIndex < totalNumberOfFrames; frameIndex++) {
            long timestampBeforeProcessing = timeSource.currentTimeNano();

            final BufferedImage screen = imageInput.readImage();
            System.out.println("Snap ! " + frameIndex);

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
            double timeBetweenFramesMillis = inputFrameRate.getValue() * 1000;
            long nextTimestamp = timestampBeforeProcessing + TimeUnit.MILLISECONDS.toNanos((long) timeBetweenFramesMillis);
            timeSource.wakeUpAt(nextTimestamp, TimeUnit.NANOSECONDS);
        }

        /*
          Flush the encoder by writing data until we get a new (incomplete) packet
         */
        do {
            encoder.encode(packet, null);
            if (packet.isComplete())
                muxer.write(packet, false);
        } while (packet.isComplete());
    }

    public void close() throws IOException, InterruptedException {
        imageInput.close();
        muxer.close();
    }
}
