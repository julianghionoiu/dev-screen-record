package record;

import org.junit.Ignore;
import org.junit.Test;
import record.image.output.OutputToScreen;
import record.video.VideoPlayer;

import java.util.concurrent.TimeUnit;

public class PlayerAcceptanceTest {

    @Test
    @Ignore("Manual acceptance")
    public void play_recorded_video() throws Exception {
        VideoPlayer videoPlayer = new VideoPlayer(new OutputToScreen());

        videoPlayer.open("./build/text.mp4");
        videoPlayer.seekTo(2, TimeUnit.SECONDS);
        videoPlayer.seekTo(5, TimeUnit.SECONDS);
        videoPlayer.play();

        videoPlayer.close();
    }


}