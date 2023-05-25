package org.tsd.rest.v1.tsdtv.job;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class TSDTVStopJob extends Job {
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("agentId", agentId)
                .append("timeoutMillis", timeoutMillis)
                .toString();
    }
}
