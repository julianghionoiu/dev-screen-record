package input_from_screen;

import org.junit.Test;
import tdl.record.screen.image.input.ImageInput;
import tdl.record.screen.image.input.InputFromStaticImage;
import tdl.record.screen.image.input.InputFromStaticImageWithCustomDimensions;
import tdl.record.screen.video.VideoRecorder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static junit.framework.TestCase.fail;

public class CanVideoRecorderPlayWithDiffScreenMetrics {

    private static final int ODD_WIDTH = 3843;
    private static final int EVEN_HEIGHT = 2160;
    private static final int EVEN_WIDTH = 3840;
    private static final int ODD_HEIGHT = 2163;

    private final String referenceImage = "src/test/resources/4k_wallpaper.jpg";

    @Test public void
    given_screen_width_is_odd_adjust_width_and_video_should_still_play_fine() {
        try {
            ImageInput imageInput = new InputFromStaticImageWithCustomDimensions(
                    new InputFromStaticImage(referenceImage),
                    ODD_WIDTH,
                    EVEN_HEIGHT
            );

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
            ImageInput imageInput = new InputFromStaticImageWithCustomDimensions(
                    new InputFromStaticImage(referenceImage),
                    EVEN_WIDTH,
                    EVEN_HEIGHT
            );

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
            ImageInput imageInput = new InputFromStaticImageWithCustomDimensions(
                    new InputFromStaticImage(referenceImage),
                    EVEN_WIDTH,
                    ODD_HEIGHT
            );

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
