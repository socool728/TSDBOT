package org.tsd.rest.v1.tsdtv.job;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class TSDTVPlayJobResult extends JobResult {
    private boolean success;
    private long timeStarted;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(long timeStarted) {
        this.timeStarted = timeStarted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("success", success)
                .append("timeStarted", timeStarted)
                .append("jobId", jobId)
                .toString();
    }
}
