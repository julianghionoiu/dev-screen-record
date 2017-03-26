package tdl.record;

import tdl.record.image.input.InputFromScreen;
import tdl.record.image.input.ScaleToOptimalSizeImage;
import tdl.record.utils.ImageQualityHint;
import tdl.record.video.VideoRecorder;
import tdl.record.video.VideoRecorderException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ScreenRecorderCliApp {

    public static void main(String[] args) throws VideoRecorderException {



        VideoRecorder videoRecorder = new VideoRecorder
                .Builder(new ScaleToOptimalSizeImage(ImageQualityHint.HIGH, new InputFromScreen()))
                .build();

        videoRecorder.open("text.mp4", 4, 4);
        videoRecorder.record(Duration.of(1, ChronoUnit.MINUTES));

        videoRecorder.close();

    }
}
