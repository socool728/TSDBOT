package org.tsd.tsdbot.tsdtv;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.time.Instant;

public class TSDTVViewer implements Serializable {
    private final String viewerUuid;
    private Instant lastRecorded;

    public TSDTVViewer(String viewerUuid, Instant lastRecorded) {
        this.viewerUuid = viewerUuid;
        this.lastRecorded = lastRecorded;
    }

    public String getViewerUuid() {
        return viewerUuid;
    }

    public Instant getLastRecorded() {
        return lastRecorded;
    }

    public void setLastRecorded(Instant lastRecorded) {
        this.lastRecorded = lastRecorded;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("viewerUuid", viewerUuid)
                .append("lastRecorded", lastRecorded)
                .toString();
    }
}
