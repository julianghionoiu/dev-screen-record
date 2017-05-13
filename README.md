[![Java Version](http://img.shields.io/badge/Java-1.8-blue.svg)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
[![Latest release](https://img.shields.io/bintray/v/julianghionoiu/maven/dev-screen-record.svg)](https://bintray.com/julianghionoiu/maven/dev-screen-record/_latestVersion)
[![Codeship Status for julianghionoiu/dev-screen-record](https://img.shields.io/codeship/dcd3e060-eb2a-0134-19b1-12840b09bc35/master.svg)](https://codeship.com/projects/207991)
[![Coverage Status](https://coveralls.io/repos/github/julianghionoiu/dev-screen-record/badge.svg?branch=master)](https://coveralls.io/github/julianghionoiu/dev-screen-record?branch=master)

Library designed for recording programming sessions.
The video generated is a MP4 file enabled for streaming. (Fragmented MP4, 5 min fragments)

## To use as a library

### Add as Maven dependency

Add a dependency to `tdl:dev-screen-record` in `compile` scope. See top of README for latest release.
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

### Configure Input and Output source

**TODO** Add examples

## Development

### Build and run as command-line app

This will grate a maven based Jar that will download the required dependencies before running the app:
```
./gradlew mavenCapsule
java -jar ./build/libs/dev-screen-record-1.0-SNAPSHOT-capsule.jar --duration 1 --output ./recording.mp4
```

### Install to mavenLocal

If you want to build the SNAPSHOT version locally you can install to the local Maven cache
```
./gradlew -x test install
```

### Release to jcenter and mavenCentral

The CI server is configured to pushs release branches to Bintray.
You trigger the process by running the `release` command locally. 

The command will increment the release number and create and annotated tag:
```bash
./gradlew release
git push --tags
```