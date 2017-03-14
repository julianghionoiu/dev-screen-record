package tdl.record.time;

import java.util.concurrent.TimeUnit;

public class SystemTimeSource implements TimeSource {


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
            System.out.println("Sleep for: " + timeToSleepMillis);
            Thread.sleep(timeToSleepMillis);
        }
    }
}
