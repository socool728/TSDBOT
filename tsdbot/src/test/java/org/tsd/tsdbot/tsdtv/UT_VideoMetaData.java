package org.tsd.tsdbot.tsdtv;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcjinfo.MediaInfo;
import uk.co.caprica.vlcjinfo.binding.LibMediaInfo;

@RunWith(MockitoJUnitRunner.class)
public class UT_VideoMetaData {

    private static final Logger log = LoggerFactory.getLogger(UT_VideoMetaData.class);

    @Test
    public void test() {
        // Get the meta data and dump it out

        LibMediaInfo INSTANCE = (LibMediaInfo) Native.loadLibrary(Platform.isWindows() ? "MediaInfo" : "mediainfo", LibMediaInfo.class);

        MediaInfo mediaInfo = MediaInfo.mediaInfo("C:/Users/Joe/Videos/tsdtv/01.mkv");
        log.debug("mediaMeta={}", mediaInfo);
    }
}
