package record;

import org.junit.Ignore;
import org.junit.Test;
import record.image.input.InputFromVideoFile;
import record.image.input.InputFromScreen;
import record.time.FakeTimeSource;
import record.time.TimeSource;
import record.video.VideoRecorder;

public class RecordAcceptanceTest {

    @Test
    @Ignore("Manual acceptance")
    public void record_screen_at_4x() throws Exception {
        VideoRecorder videoRecorder = new VideoRecorder(new InputFromScreen());

        videoRecorder.open("text.mp4", 4, 1);
        videoRecorder.record(30);

        videoRecorder.close();
    }

    /**
     * Records at 4x: Given screen displays stream of X..Y..Z, an mp4 movie should be produced containing XYZ at key frames
     *
     * Input:
     *  - time
     *  - screen
     *
     * Output:
     *  - file
     *
     * Tasks:
     *  - simulate time, allow Thread.sleep to fake the passing of time
     *  - simulate screen
     *      - generate an input video (either mp4 or BufferedImage array)
     *      - have the ability to retrieve the frame based on timestamp
     *      - connect simulated screen with simulated time
     *  - check the existence of key frames in the video output
     *      - use a real video (normal speed and 30fps) as input and perform a decode on it: https://github.com/artclarke/humble-video/blob/master/humble-video-demos/src/main/java/io/humble/video/demos/DecodeAndPlayVideo.java
     *      - check our barcode generation: 'net.glxn.qrgen.javase'
     *      - maybe by generating and reading barcodes ? https://github.com/zxing/zxing
     */
    @Test
    public void can_record_video() throws Exception {
        TimeSource timeSource = new FakeTimeSource();
        InputFromVideoFile imageInput = new InputFromVideoFile("src/test/resources/t_reference_recording.mp4", timeSource);
        VideoRecorder videoRecorder = new VideoRecorder(imageInput, timeSource);

        videoRecorder.open("text.mp4", 20, 1);
        videoRecorder.record(30);

        videoRecorder.close();
    }


    /**
     * Size: Given known input containing movement, the size should be less than X
     */
    @Test
    @Ignore("Not implemented")
    public void size_should_be_kept_small_while_retaining_quality() throws Exception {

    }

    /**
     * Packets produced: The video should be broken down in discrete packets to make upload easier. (write every X min)
     */
    @Test
    @Ignore("Not implemented")
    public void video_should_be_broken_down_into_discrete_packets_to_help_with_upload() throws Exception {

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