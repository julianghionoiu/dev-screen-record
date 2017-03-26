package tdl.record;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import tdl.record.image.input.InputFromScreen;
import tdl.record.image.input.ScaleToOptimalSizeImage;
import tdl.record.utils.ImageQualityHint;
import tdl.record.video.VideoRecorder;
import tdl.record.video.VideoRecorderException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ScreenRecorderCliApp {
    @Parameter(names = {"-d", "-destination"}, description = "The path to the recording file")
    private String destinationPath = "./recording.mp4";

    @Parameter(names = {"-t", "-time"}, description = "Duration of the recording in minutes. Pass -1 for continuous recording.")
    private Integer recordingTime = -1;


    public static void main(String[] args) throws VideoRecorderException {
        ScreenRecorderCliApp main = new ScreenRecorderCliApp();
        new JCommander(main, args);
        main.run();
    }

    private void run() throws VideoRecorderException {
        VideoRecorder videoRecorder = new VideoRecorder
                .Builder(new ScaleToOptimalSizeImage(ImageQualityHint.MEDIUM, new InputFromScreen()))
                .build();

        if (recordingTime < 0) {
            throw new IllegalArgumentException("Continuous recording not implemented");
        }
        videoRecorder.open(destinationPath, 4, 4);
        videoRecorder.record(Duration.of(1, ChronoUnit.MINUTES));

        videoRecorder.close();
    }
}
