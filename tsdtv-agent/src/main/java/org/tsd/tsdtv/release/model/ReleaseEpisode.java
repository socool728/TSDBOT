package org.tsd.tsdtv.release.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tsd.tsdtv.release.Release;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class ReleaseEpisode implements Serializable {

    private String guid;
    private int episodeNumber;
    private String fullName;
    private Date releaseDate;
    private String pathToFile;

    public ReleaseEpisode() {
    }

    public ReleaseEpisode(Release release, File file) {
        this.guid = release.getGuid();
        this.episodeNumber = release.getEpisodeNumber();
        this.fullName = release.getEpisodeName();
        this.releaseDate = null;
        this.pathToFile = file.getAbsolutePath();
    }

    public String getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("guid", guid)
                .append("episodeNumber", episodeNumber)
                .append("fullName", fullName)
                .append("releaseDate", releaseDate)
                .append("pathToFile", pathToFile)
                .toString();
    }
}
