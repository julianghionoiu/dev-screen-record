package tdl.record.metrics;

import io.humble.video.Rational;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class RecordingMetricsCollector implements RecordingListener {
    private long expectedTimeBetweenFramesNanos;
    private long timestampBeforeProcessingNanos;
    private double renderingTimeRatio;
    private long totalFrames;
    private Rational inputFrameRate;
    private Rational videoFrameRate;


    public RecordingMetricsCollector() {
        expectedTimeBetweenFramesNanos = Long.MAX_VALUE;
        inputFrameRate = Rational.make();
        videoFrameRate = Rational.make();
        renderingTimeRatio = 0;
        timestampBeforeProcessingNanos = 0;
        totalFrames = 0;
    }

    //~~~~~~~~~~ Collectors

    @Override
    public void setFrameRates(Rational inputFrameRate, Rational videoFrameRate) {
        this.inputFrameRate = inputFrameRate;
        this.videoFrameRate = videoFrameRate;
        double timeBetweenFramesMillis = inputFrameRate.getValue() * 1000;
        this.expectedTimeBetweenFramesNanos = TimeUnit.MILLISECONDS.toNanos((long) timeBetweenFramesMillis);
    }

    @Override
    public void notifyFrameRenderingStarts(long timestamp, TimeUnit unit, long frameIndex) {
        log.debug("Snap ! {}", frameIndex);
        timestampBeforeProcessingNanos = unit.toNanos(timestamp);
    }

    @Override
    public void notifyFrameRenderingEnds(long timestamp, TimeUnit unit, long frameIndex) {
        long timeSpendProcessingNanos = unit.toNanos(timestamp) - timestampBeforeProcessingNanos;
        renderingTimeRatio = timeSpendProcessingNanos / (double) expectedTimeBetweenFramesNanos;
        log.debug("renderingTimeRatio: {}", renderingTimeRatio);
        totalFrames = frameIndex;
    }

    //~~~~~~~~~~ Getters

    public Rational getInputFrameRate() {
        return inputFrameRate;
    }

    public Rational getVideoFrameRate() {
        return videoFrameRate;
    }

    /**
     * @return the percentage of time spent processing vs the percentage of time spent for a frame
     */
    public double getRenderingTimeRatio() {
        return renderingTimeRatio;
    }

    /**
     * @return total number of processed frames
     */
    public long getTotalFrames() {
        return totalFrames;
    }
}
