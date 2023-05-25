package org.tsd.rest.v1.tsdtv;

import org.tsd.rest.v1.tsdtv.stream.AudioStream;
import org.tsd.rest.v1.tsdtv.stream.SubtitleStream;
import org.tsd.rest.v1.tsdtv.stream.VideoStream;

import java.util.LinkedList;
import java.util.List;

public class MediaInfo {

    private String filePath;
    private long fileSize;
    private int durationSeconds;
    private long bitRate;
    private List<VideoStream> videoStreams = new LinkedList<>();
    private List<AudioStream> audioStreams = new LinkedList<>();
    private List<SubtitleStream> subtitleStreams = new LinkedList<>();

    public long getBitRate() {
        return bitRate;
    }

    public void setBitRate(long bitRate) {
        this.bitRate = bitRate;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public List<VideoStream> getVideoStreams() {
        return videoStreams;
    }

    public void setVideoStreams(List<VideoStream> videoStreams) {
        this.videoStreams = videoStreams;
    }

    public List<AudioStream> getAudioStreams() {
        return audioStreams;
    }

    public void setAudioStreams(List<AudioStream> audioStreams) {
        this.audioStreams = audioStreams;
    }

    public List<SubtitleStream> getSubtitleStreams() {
        return subtitleStreams;
    }

    public void setSubtitleStreams(List<SubtitleStream> subtitleStreams) {
        this.subtitleStreams = subtitleStreams;
    }
}
