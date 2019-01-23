package input_from_screen;

import org.junit.Test;
import tdl.record.screen.image.input.ImageInput;
import tdl.record.screen.image.input.InputFromStaticImage;
import tdl.record.screen.image.input.ScaleToOptimalSizeImage;
import tdl.record.screen.utils.ImageQualityHint;
import tdl.record.screen.video.VideoRecorder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static junit.framework.TestCase.fail;

public class CanVideoRecorderPlayWithDiffScreenMetrics {

    @Test public void
    given_screen_width_is_odd_adjust_width_and_video_should_still_play_fine() {
        try {
            String referenceImageOddWidth = "src/test/resources/4k_wallpaper_odd_width.jpg";
            ImageInput imageInput = new ScaleToOptimalSizeImage(
                    ImageQualityHint.HIGH,
                    new InputFromStaticImage(referenceImageOddWidth)
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
            String referenceImage = "src/test/resources/4k_wallpaper.jpg";
            ImageInput imageInput = new ScaleToOptimalSizeImage(
                    ImageQualityHint.MEDIUM, new InputFromStaticImage(referenceImage)
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
            String referenceImageOddHeight = "src/test/resources/4k_wallpaper_odd_height.jpg";
            ImageInput imageInput = new ScaleToOptimalSizeImage(
                    ImageQualityHint.LOW, new InputFromStaticImage(referenceImageOddHeight)
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
