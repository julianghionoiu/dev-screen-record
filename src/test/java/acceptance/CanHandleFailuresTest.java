package acceptance;

import com.google.zxing.BarcodeFormat;
import org.junit.Ignore;
import org.junit.Test;
import tdl.record.image.input.ImageInput;
import tdl.record.image.input.InputFromFaultySource;
import tdl.record.image.input.InputFromStreamOfBarcodes;
import tdl.record.image.output.OutputToInMemoryBuffer;
import tdl.record.time.FakeTimeSource;
import tdl.record.time.TimeSource;
import tdl.record.video.VideoPlayer;
import tdl.record.video.VideoRecorder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class CanHandleFailuresTest {

    @Test
    @Ignore("Manual test")
    public void recording_should_stop_gracefully_on_sigterm() throws Exception {
        //Start recording
        //Send SIGTERM (Ctrl+C)
        //Check that the video produced is still usable
    }

    @Test
    public void recording_should_stop_gracefully_on_fatal_exception() throws Exception {
        String destinationVideo = "build/recording_interrupted_by_exception.mp4";
        TimeSource recordTimeSource = new FakeTimeSource();
        ImageInput imageInput = new InputFromStreamOfBarcodes(BarcodeFormat.CODE_39, 300, 150, recordTimeSource);
        VideoRecorder videoRecorder = new VideoRecorder
                .Builder(new InputFromFaultySource(10, imageInput))
                .withTimeSource(recordTimeSource).build();

        // Capture video
        videoRecorder.open(destinationVideo, 5, 1);
        try {
            videoRecorder.start(Duration.of(60, ChronoUnit.SECONDS));
        } catch (Exception e) {
            System.out.println("Exception caught and ignored: "+e.getMessage());
        }
        videoRecorder.close();

        // Read recorded video parameters
        VideoPlayer videoPlayer = new VideoPlayer(new OutputToInMemoryBuffer(), recordTimeSource);
        videoPlayer.open(destinationVideo);
        assertThat("Video duration is not as expected", videoPlayer.getDuration(), is(Duration.of(2, ChronoUnit.SECONDS)));
    }

    /**
     * Packets produced: The video should be broken down in discrete packets to make upload easier. (write every X min)
     */
    @Test
    @Ignore("Not implemented")
    public void video_should_be_broken_down_into_discrete_packets_to_help_with_upload() throws Exception {
        //TODO Before doing this, play a spike. What is the best way to upload a file?
        //TODO One big file + Multipart, or multiple small files.

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