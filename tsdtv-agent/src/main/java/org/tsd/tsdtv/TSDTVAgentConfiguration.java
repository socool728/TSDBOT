package org.tsd.tsdtv;

import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;
import org.tsd.app.config.FfmpegConfig;
import org.tsd.tsdtv.release.ReleaseSource;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public class TSDTVAgentConfiguration extends Configuration {

    @NotNull
    @NotEmpty
    private String agentId;

    @NotNull
    @NotEmpty
    private String tsdbotUrl;

    @NotNull
    @NotEmpty
    private String inventoryPath;

    @NotNull
    @NotEmpty
    private String password;

    @NotNull
    @Valid
    private FfmpegConfig ffmpeg;

    @NotNull
    @NotEmpty
    private String releasesDirectory;

    @NotNull
    private Map<ReleaseSource, List<String>> releases;

    public Map<ReleaseSource, List<String>> getReleases() {
        return releases;
    }

    public void setReleases(Map<ReleaseSource, List<String>> releases) {
        this.releases = releases;
    }

    public String getReleasesDirectory() {
        return releasesDirectory;
    }

    public void setReleasesDirectory(String releasesDirectory) {
        this.releasesDirectory = releasesDirectory;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTsdbotUrl() {
        return tsdbotUrl;
    }

    public void setTsdbotUrl(String tsdbotUrl) {
        this.tsdbotUrl = tsdbotUrl;
    }

    public String getInventoryPath() {
        return inventoryPath;
    }

    public void setInventoryPath(String inventoryPath) {
        this.inventoryPath = inventoryPath;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public FfmpegConfig getFfmpeg() {
        return ffmpeg;
    }

    public void setFfmpeg(FfmpegConfig ffmpeg) {
        this.ffmpeg = ffmpeg;
    }
}
