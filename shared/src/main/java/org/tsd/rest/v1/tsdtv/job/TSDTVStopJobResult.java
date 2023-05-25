package org.tsd.rest.v1.tsdtv.job;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class TSDTVStopJobResult extends JobResult {

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jobId", jobId)
                .toString();
    }
}
