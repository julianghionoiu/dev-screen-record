package tdl.record;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.extern.slf4j.Slf4j;
import tdl.record.image.input.InputFromScreen;
import tdl.record.image.input.ScaleToOptimalSizeImage;
import tdl.record.utils.ImageQualityHint;
import tdl.record.video.VideoRecorder;
import tdl.record.video.VideoRecorderException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Slf4j
public class ScreenRecorderCliApp {
    @Parameter(names = {"-o", "--output"}, description = "The path to the recording file")
    private String destinationPath = "./recording.mp4";

    @Parameter(names = {"-d", "--duration"}, description = "Duration of the recording in minutes. Pass -1 for continuous recording.")
    private Integer recordingTime = -1;


    public static void main(String[] args) throws VideoRecorderException {
        log.info("Starting recording app");
        ScreenRecorderCliApp main = new ScreenRecorderCliApp();
        new JCommander(main, args);
        main.run();
    }

    private void run() throws VideoRecorderException {
        if (recordingTime < 0) {
            throw new IllegalArgumentException("Continuous recording not implemented");
        }

        //TODO Display a status message every minute so that the user gets feedback on the recording
        VideoRecorder videoRecorder = new VideoRecorder
                .Builder(new ScaleToOptimalSizeImage(ImageQualityHint.MEDIUM, new InputFromScreen()))
                .build();
        registerShutdownHook(videoRecorder);

        videoRecorder.open(destinationPath, 4, 4);
        videoRecorder.start(Duration.of(recordingTime, ChronoUnit.MINUTES));
        videoRecorder.close();
    }

    private void registerShutdownHook(final VideoRecorder videoRecorder) {
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            videoRecorder.stop();
            if (!mainThread.isInterrupted()) {
                mainThread.interrupt();
            }

            try {
                mainThread.join();
            } catch (InterruptedException e) {
                log.warn("Could not join main thread", e);
            }
        }));
    }
}
