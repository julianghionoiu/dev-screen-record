package tdl.record.metrics;
import java.util.concurrent.TimeUnit;

public class RecorderMetricsCollector {
    private long expectedTimeBetweenFramesNanos;
    private long timestampBeforeProcessingNanos;
    private double processingRatio;

    public RecorderMetricsCollector() {
        expectedTimeBetweenFramesNanos = Long.MAX_VALUE;
        timestampBeforeProcessingNanos = 0;
        processingRatio = 0;
    }

    //~~~~~~~~~~ Collectors

    public void setExpectedTimeBetweenFrames(double timeBetweenFrames, TimeUnit unit) {
        expectedTimeBetweenFramesNanos = unit.toNanos((long) timeBetweenFrames);
    }

    public void notifyFrameStartAt(long timestampBeforeProcessing, TimeUnit unit, long frameIndex) {
        System.out.println("Snap ! " + frameIndex);
        timestampBeforeProcessingNanos = unit.toNanos(timestampBeforeProcessing);
    }

    public void notifyFrameEndAt(long timestampNow, TimeUnit unit, long frameIndex) {
        long timeSpendProcessingNanos = unit.toNanos(timestampNow) - timestampBeforeProcessingNanos;
        processingRatio = timeSpendProcessingNanos / (double) expectedTimeBetweenFramesNanos;
        System.out.println("processingRatio: "+processingRatio);
    }

    //~~~~~~~~~~ Getters

    /**
     * @return the percentage of time spent processing vs the percentage of time spent for a frame
     */
    public double getProcessingRatio() {
        return processingRatio;
    }
}
