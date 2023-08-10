[![Java Version](http://img.shields.io/badge/Java-1.8-blue.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
[![Download](https://api.bintray.com/packages/julianghionoiu/maven/dev-screen-record/images/download.svg)](https://bintray.com/julianghionoiu/maven/dev-screen-record/_latestVersion)
[![Codeship Status for julianghionoiu/dev-screen-record](https://img.shields.io/codeship/dcd3e060-eb2a-0134-19b1-12840b09bc35/master.svg)](https://codeship.com/projects/207991)
[![Coverage Status](https://img.shields.io/codecov/c/github/julianghionoiu/dev-screen-record.svg)](https://codecov.io/gh/julianghionoiu/dev-screen-record)
 
Library designed for recording programming sessions.
The video generated is a MP4 file enabled for streaming. (Fragmented MP4, 5 min fragments)

## To use as a library

### Add as Maven dependency

Add a dependency to `tdl:dev-screen-record` in `compile` scope. See `bintray` shield for latest release number.
```xml
<dependency>
  <groupId>ro.ghionoiu</groupId>
  <artifactId>dev-screen-record</artifactId>
  <version>X.Y.Z</version>
</dependency>
```

The library uses `humble-video` which wraps `ffmpeg`. You need to include the binary for your platform
```properties
windows-x86:  io.humble:humble-video-arch-x86_64-w64-mingw32:0.2.1
windows-i686: io.humble:humble-video-arch-i686-w64-mingw32:0.2.1
macos-x86:    io.humble:humble-video-arch-x86_64-apple-darwin12:0.2.1
macos-i686:   io.humble:humble-video-arch-i686-apple-darwin12:0.2.1
linux-x86:    io.humble:humble-video-arch-x86_64-pc-linux-gnu6:0.2.1
linux-i686:   io.humble:humble-video-arch-i686-pc-linux-gnu6:0.2.1
```

Example:
```xml
<dependency>
  <groupId>io.humble</groupId>
  <artifactId>humble-video-arch-x86_64-apple-darwin</artifactId>
  <version>0.2.1</version>
</dependency>
```

### Usage

The following example records the screen for 1 minute at 4 snapshots per second. 
The final video is speed up by a factor of 4 resulting in 16 frames per second.

```java
        String destinationPath = "./screen.mp4";
        VideoRecorder videoRecorder = new VideoRecorder
                .Builder(new ScaleToOptimalSizeImage(ImageQualityHint.MEDIUM, new InputFromScreen()))
                .build();


        int snapsPerSecond = 4;
        int timeSpeedUpFactor = 4;
        videoRecorder.open(destinationPath, snapsPerSecond, timeSpeedUpFactor);
        videoRecorder.start(Duration.of(1, ChronoUnit.MINUTES)); //Will block
        videoRecorder.close();
```

To monitor the **recording progress** you register a `RecordingListener`. 
The following example displays the metrics to the screen every 5 seconds:

```java
        VideoRecordingMetricsCollector videoRecordingMetricsCollector = new VideoRecordingMetricsCollector();
        VideoRecorder videoRecorder = new VideoRecorder
                .Builder(new ScaleToOptimalSizeImage(ImageQualityHint.MEDIUM, new InputFromScreen()))
                .withRecordingListener(videoRecordingMetricsCollector)
                .build();
        
        //Issue performance updates
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Recorded "+videoRecordingMetricsCollector.getTotalFrames() + " frames"
                        +" at "+ videoRecordingMetricsCollector.getVideoFrameRate().getDenominator() + " fps"
                        +" with a load of " + videoRecordingMetricsCollector.getRenderingTimeRatio());
            }
        }, 0, 5000);
```

To **gracefully stop the recording** you must ensure that you call the `stop()` on the recording.
You do this by registering `shutdownHook`:
```java
        final Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            videoRecorder.stop();
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                log.warn("Could not join main thread", e);
            }
        }));
```


## Development

### Build and run as command-line app

This will create a Jar file with the platform specific video library embedded
```
./gradlew build shadowJar -i -PvideoArch=macos
java -Dlogback.configurationFile=`pwd`/logback.xml \
    -jar ./build/libs/dev-screen-record-macos-all.jar --duration 1 --output ./recording.mp4
```

### Install to mavenLocal

If you want to build the SNAPSHOT version locally you can install to the local Maven cache
```
./gradlew -x test clean install
```

#### JUnit tests

```bash
./gradlew clean test -i
```

### Publish to Maven Central

Publish to Maven Central Staging repo
```bash
./gradlew publish
```

A Staging repository is created automatically:
https://oss.sonatype.org/#stagingRepositories

To promote to the Live repo, do the following:
- "Close" the Staging repo, Sonatype Lift will scan the repo for vuln, check the email
- "Refresh" the Staging repos
- "Release" the repo
- wait between 15 mins and up to 2 hours for the new version to appear in Central
- first check the Web UI: https://oss.sonatype.org/#view-repositories;releases~browsestorage
- then check: https://repo1.maven.org/maven2/ro/ghionoiu/dev-screen-record/
