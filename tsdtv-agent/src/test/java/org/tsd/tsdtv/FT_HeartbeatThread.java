package org.tsd.tsdtv;

import net.bramp.ffmpeg.FFprobe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class FT_HeartbeatThread {

    @Mock
    private TSDBotClient tsdBotClient;

    @Mock
    private NetworkMonitor networkMonitor;

    private File inventoryDirectory;
    private FFprobe fFprobe;
    private HeartbeatThread heartbeatThread;

    @Before
    public void setup() throws IOException {
//        fFprobe = new FFprobe("C:\\ffmpeg-3.3.3-win64-static\\bin\\ffprobe.exe");
//        inventoryDirectory = new File("C:/Users/Joe/Videos/tsdtv");
//        heartbeatThread = new HeartbeatThread(tsdBotClient, fFprobe, networkMonitor, "agentId", inventoryDirectory);
    }

    @Test
    public void testRun() {
        heartbeatThread.run();
    }
}
