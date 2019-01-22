package acceptance;

import com.google.zxing.BarcodeFormat;
import org.junit.Test;
import tdl.record.screen.image.input.ImageInput;
import tdl.record.screen.image.input.InputFromStreamOfBarcodes;
import tdl.record.screen.time.FakeTimeSource;
import tdl.record.screen.time.TimeSource;
import tdl.record.screen.video.VideoRecorder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static junit.framework.TestCase.fail;

public class CanVideoRecorderPlayWithDiffScreenMetrics {

    private static final int EVEN_WIDTH = 300;
    private static final int ODD_HEIGHT = 151;
    private static final int ODD_WIDTH = 301;
    private static final int EVEN_HEIGHT = 150;

    @Test public void
    given_screen_width_is_odd_adjust_width_and_video_should_still_play_fine() {
        try {
            TimeSource recordTimeSource = new FakeTimeSource();
            ImageInput imageInput = new InputFromStreamOfBarcodes(BarcodeFormat.CODE_39, ODD_WIDTH, EVEN_HEIGHT, recordTimeSource);

            VideoRecorder videoRecorder = new VideoRecorder
                    .Builder(imageInput)
                    .build();
            videoRecorder.open("screen_width_is_odd.mp4", 4, 4);
            videoRecorder.start(Duration.of(2, ChronoUnit.SECONDS));

            videoRecorder.close();
        } catch (Exception ex) {
            fail("Video should play perfectly fine, instead threw an exception: " + ex.getMessage());
        }
    }

    @Test public void
    given_screen_height_and_width_are_even_and_video_should_play_fine() {
        try {
            TimeSource recordTimeSource = new FakeTimeSource();
            ImageInput imageInput = new InputFromStreamOfBarcodes(BarcodeFormat.CODE_39, EVEN_WIDTH, EVEN_HEIGHT, recordTimeSource);

            VideoRecorder videoRecorder = new VideoRecorder
                    .Builder(imageInput)
                    .build();
            videoRecorder.open("screen_height_and_width_are_even.mp4", 4, 4);
            videoRecorder.start(Duration.of(2, ChronoUnit.SECONDS));

            videoRecorder.close();
        } catch (Exception ex) {
            fail("Video should play perfectly fine, instead threw an exception: " + ex.getMessage());
        }
    }


    @Test public void
    given_screen_height_is_odd_adjust_height_and_video_should_still_play_fine() {
        try {
            TimeSource recordTimeSource = new FakeTimeSource();
            ImageInput imageInput = new InputFromStreamOfBarcodes(BarcodeFormat.CODE_39, EVEN_WIDTH, ODD_HEIGHT, recordTimeSource);

            VideoRecorder videoRecorder = new VideoRecorder
                    .Builder(imageInput)
                    .build();
            videoRecorder.open("screen_height_is_odd_adjust_height.mp4", 4, 4);
            videoRecorder.start(Duration.of(2, ChronoUnit.SECONDS));

            videoRecorder.close();
        } catch (Exception ex) {
            fail("Screen height should have been adjusted and continued to play, instead threw an exception: " + ex.getMessage());
        }
    }
}
