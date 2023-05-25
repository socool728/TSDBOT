package org.tsd.rest.v1.tsdtv.stream;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class VideoStream extends Stream {
    private int width;
    private int height;
    private String sampleAspectRatio;
    private String displayAspectRatio;
    private String pixFmt;
    private boolean isAvc;
    private double rFrameRate;
    private double avgFrameRate;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getSampleAspectRatio() {
        return sampleAspectRatio;
    }

    public void setSampleAspectRatio(String sampleAspectRatio) {
        this.sampleAspectRatio = sampleAspectRatio;
    }

    public String getDisplayAspectRatio() {
        return displayAspectRatio;
    }

    public void setDisplayAspectRatio(String displayAspectRatio) {
        this.displayAspectRatio = displayAspectRatio;
    }

    public String getPixFmt() {
        return pixFmt;
    }

    public void setPixFmt(String pixFmt) {
        this.pixFmt = pixFmt;
    }

    public boolean isAvc() {
        return isAvc;
    }

    public void setAvc(boolean avc) {
        isAvc = avc;
    }

    public double getrFrameRate() {
        return rFrameRate;
    }

    public void setrFrameRate(double rFrameRate) {
        this.rFrameRate = rFrameRate;
    }

    public double getAvgFrameRate() {
        return avgFrameRate;
    }

    public void setAvgFrameRate(double avgFrameRate) {
        this.avgFrameRate = avgFrameRate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("width", width)
                .append("height", height)
                .append("sampleAspectRatio", sampleAspectRatio)
                .append("displayAspectRatio", displayAspectRatio)
                .append("pixFmt", pixFmt)
                .append("isAvc", isAvc)
                .append("rFrameRate", rFrameRate)
                .append("avgFrameRate", avgFrameRate)
                .append("index", index)
                .append("codecName", codecName)
                .append("tags", tags)
                .toString();
    }
}
