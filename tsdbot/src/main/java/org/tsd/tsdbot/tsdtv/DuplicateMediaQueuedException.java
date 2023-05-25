package org.tsd.tsdbot.tsdtv;

public class DuplicateMediaQueuedException extends TSDTVException {
    public DuplicateMediaQueuedException(String agentId, int mediaId) {
        super("Media already in queue, agentId = "+agentId+", mediaId="+mediaId);
    }
}
