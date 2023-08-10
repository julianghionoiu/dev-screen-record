package tdl.record.screen.time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class SystemTimeSource implements TimeSource {
    private static final Logger log = LoggerFactory.getLogger(SystemTimeSource.class);

    @Override
    public long currentTimeNano() {
        return System.nanoTime();
    }


    @Override
    public void wakeUpAt(long timestamp, TimeUnit timeUnit) throws InterruptedException {
        long currentTimestampNano = currentTimeNano();
        long targetTimestampNano = timeUnit.toNanos(timestamp);

        long timeToSleepMillis = TimeUnit.NANOSECONDS
                .toMillis(targetTimestampNano - currentTimestampNano);

        if (timeToSleepMillis > 1) {
            log.debug("Sleep for: {} millis", timeToSleepMillis);
            Thread.sleep(timeToSleepMillis);
        }
    }
}
