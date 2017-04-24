package tdl.record.metrics;

import io.humble.video.Rational;

import java.util.concurrent.TimeUnit;

public interface RecordingListener {

    void setFrameRates(Rational inputFrameRate, Rational videoFrameRate);

    void notifyFrameRenderingStarts(long timestamp, TimeUnit unit, long frameIndex);

    void notifyFrameRenderingEnds(long timestamp, TimeUnit unit, long frameIndex);
}
