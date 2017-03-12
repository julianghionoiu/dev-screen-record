package record;

import org.junit.Ignore;
import org.junit.Test;
import record.image.input.ImageInput;
import record.image.input.InputFromScreen;
import record.image.input.InputFromStreamOfBarcodes;
import record.image.input.InputFromVideoFile;
import record.image.output.OutputToBarcodeReader;
import record.time.FakeTimeSource;
import record.time.TimeSource;
import record.video.VideoPlayer;
import record.video.VideoRecorder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class RecordAcceptanceTest {

    @Test
    @Ignore("Manual acceptance")
    public void record_screen_at_4x() throws Exception {
        VideoRecorder videoRecorder = new VideoRecorder(new InputFromScreen());

        videoRecorder.open("text.mp4", 4, 1);
        videoRecorder.record(Duration.of(30, ChronoUnit.SECONDS));

        videoRecorder.close();
    }

    /**
     * Records at 4x: Given screen displays stream of X..Y..Z, an mp4 movie should be produced containing XYZ at key frames
     * <p>
     * Input:
     * - time
     * - screen
     * <p>
     * Output:
     * - file
     * <p>
     * Tasks:
     * - simulate time, allow Thread.sleep to fake the passing of time
     * - simulate screen
     * - generate an input video (either mp4 or BufferedImage array)
     * - have the ability to retrieve the frame based on timestamp
     * - connect simulated screen with simulated time
     * - check the existence of key frames in the video output
     * - use a real video (normal speed and 30fps) as input and perform a decode on it: https://github.com/artclarke/humble-video/blob/master/humble-video-demos/src/main/java/io/humble/video/demos/DecodeAndPlayVideo.java
     * - check our barcode generation: 'net.glxn.qrgen.javase'
     * - maybe by generating and reading barcodes ? https://github.com/zxing/zxing
     */
    @Test
    public void can_record_video_at_specified_frame_rate_and_speed() throws Exception {
        String outputFile = "./build/text.mp4";
        TimeSource recordTimeSource = new FakeTimeSource();
        ImageInput imageInput = new InputFromStreamOfBarcodes(300, 150, recordTimeSource);
        VideoRecorder videoRecorder = new VideoRecorder(imageInput, recordTimeSource);

        // Capture video
        videoRecorder.open(outputFile, 20, 1);
        videoRecorder.record(Duration.of(10, ChronoUnit.SECONDS));
        videoRecorder.close();

        // Read recorded video parameters
        TimeSource replayTimeSource = new FakeTimeSource();
        OutputToBarcodeReader barcodeReader = new OutputToBarcodeReader(replayTimeSource);
        VideoPlayer videoPlayer = new VideoPlayer(barcodeReader, replayTimeSource);
        videoPlayer.open(outputFile);
        assertThat(videoPlayer.getDuration(), is(Duration.of(10, ChronoUnit.SECONDS)));
        assertThat(videoPlayer.getFrameRate(), is(closeTo(20, 0.01)));
        assertThat(videoPlayer.getWidth(), is(300));
        assertThat(videoPlayer.getHeight(), is(150));

        // Play the recorded video and read the barcodes
        videoPlayer.play();
        videoPlayer.close();

        //TODO Assert that the barcodes are consistent with the speed rate
        //noinspection ResultOfMethodCallIgnored
        barcodeReader.getDecodedBarcodes();
    }


    /**
     * Size: Given known input containing movement, the size should be less than X
     */
    @Test
    @Ignore("Not implemented")
    public void size_should_be_kept_small_while_retaining_quality() throws Exception {
        TimeSource timeSource = new FakeTimeSource();
        InputFromVideoFile imageInput = new InputFromVideoFile("src/test/resources/t_reference_recording.mp4", timeSource);
        VideoRecorder videoRecorder = new VideoRecorder(imageInput, timeSource);

        videoRecorder.open("./build/text.mp4", 20, 1);
        videoRecorder.record(Duration.of(30, ChronoUnit.SECONDS));

        videoRecorder.close();
    }


    /**
     * Aspect ratio and scaling:
     * TODO: Check VIMEO requirements, do we need to change the aspect ratio?
     */
    @Test
    @Ignore("Not implemented")
    public void large_screen_size_should_be_scaled_down() throws Exception {
        //Generate large frames

        //Assert that frames are smaller
    }

    /**
     * Packets produced: The video should be broken down in discrete packets to make upload easier. (write every X min)
     */
    @Test
    @Ignore("Not implemented")
    public void video_should_be_broken_down_into_discrete_packets_to_help_with_upload() throws Exception {

        //Check generated files and assert frames contain the right timestamp
    }

    /**
     * UX: feedback should be collected and displayed on the console ( recording status, size generated, growth rate, size available )
     */
    @Test
    @Ignore("Not implemented")
    public void display_processing_feedback_to_the_console() throws Exception {

    }

    /**
     * Add 4x watermark
     */
    @Test
    @Ignore("Not implemented")
    public void the_frames_should_contain_a_watermark_with_the_speedup() throws Exception {

    }

}