package org.tsd.tsdbot.tsdtv;

public class MediaNotFoundException extends TSDTVException {
    public MediaNotFoundException(String agentId, int mediaId) {
        super("Media not found, agentId = "+agentId+", mediaId="+mediaId);
    }

    public MediaNotFoundException(int mediaId) {
        super("Media not found, mediaId="+mediaId);
    }
}
