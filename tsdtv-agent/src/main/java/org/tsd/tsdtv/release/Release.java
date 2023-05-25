package org.tsd.tsdtv.release;

public abstract class Release {
    protected String guid;
    protected String magnetUri;
    protected String torrentUri;

    public Release(String guid, String magnetUri, String torrentUri) {
        this.guid = guid;
        this.magnetUri = magnetUri;
        this.torrentUri = torrentUri;
    }

    public abstract String getSeriesName();
    public abstract String getEpisodeName();
    public abstract int getEpisodeNumber();
    public abstract ReleaseSource getReleaseSource();

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getMagnetUri() {
        return magnetUri;
    }

    public void setMagnetUri(String magnetUri) {
        this.magnetUri = magnetUri;
    }

    public String getTorrentUri() {
        return torrentUri;
    }

    public void setTorrentUri(String torrentUri) {
        this.torrentUri = torrentUri;
    }
}
