package record.image.input;

import record.image.output.OutputToInMemoryBuffer;
import record.time.TimeSource;
import record.video.VideoPlayer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class InputFromVideoFile implements ImageInput {
    private final String videoUrl;
    private final VideoPlayer videoPlayer;
    private final OutputToInMemoryBuffer inMemoryBuffer;
    private final TimeSource timeSource;
    private long systemStartTime;

    public InputFromVideoFile(String videoUrl, TimeSource timeSource) {
        this.videoUrl = videoUrl;
        this.inMemoryBuffer = new OutputToInMemoryBuffer();
        this.videoPlayer = new VideoPlayer(inMemoryBuffer, timeSource);
        this.timeSource = timeSource;
    }

    @Override
    public void open() throws AWTException, IOException, InterruptedException {
        videoPlayer.open(videoUrl);
        systemStartTime = timeSource.currentTimeNano();
    }

    @Override
    public BufferedImage readImage() throws IOException, InterruptedException {
        long currentTime = timeSource.currentTimeNano();
        videoPlayer.seekTo(currentTime - systemStartTime, TimeUnit.NANOSECONDS);
        return inMemoryBuffer.getCurrentImage();
    }

    @Override
    public BufferedImage getSampleImage() {
        return inMemoryBuffer.getSuggestedOutputSample(getWidth(), getHeight());
    }

    @Override
    public int getWidth() {
        return videoPlayer.getWidth();
    }

    @Override
    public int getHeight() {
        return videoPlayer.getHeight();
    }

    @Override
    public void close() throws IOException, InterruptedException {
        videoPlayer.close();
    }
}
