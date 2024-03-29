package acceptance;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import tdl.record.screen.image.output.OutputToScreen;
import tdl.record.screen.video.VideoPlayer;

import java.util.concurrent.TimeUnit;

public class CanPlayVideoTest {

    @Test
    @Disabled("Manual acceptance")
    public void play_recorded_video() throws Exception {
        VideoPlayer videoPlayer = new VideoPlayer(new OutputToScreen());

        videoPlayer.open("./build/text.mp4");
        videoPlayer.seekTo(2, TimeUnit.SECONDS);
        videoPlayer.seekTo(5, TimeUnit.SECONDS);
        videoPlayer.play();

        videoPlayer.close();
    }


}