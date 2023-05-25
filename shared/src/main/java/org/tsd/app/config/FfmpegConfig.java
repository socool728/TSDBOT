package org.tsd.app.config;

import javax.validation.constraints.NotNull;

public class FfmpegConfig {

    @NotNull
    private String ffmpegExec;

    @NotNull
    private String ffprobeExec;

    public String getFfmpegExec() {
        return ffmpegExec;
    }

    public void setFfmpegExec(String ffmpegExec) {
        this.ffmpegExec = ffmpegExec;
    }

    public String getFfprobeExec() {
        return ffprobeExec;
    }

    public void setFfprobeExec(String ffprobeExec) {
        this.ffprobeExec = ffprobeExec;
    }
}
