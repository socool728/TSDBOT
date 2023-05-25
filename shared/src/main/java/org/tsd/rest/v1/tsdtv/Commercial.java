package org.tsd.rest.v1.tsdtv;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;

public class Commercial extends Media {

    private String name;
    private File file;

    public Commercial() {
    }

    public Commercial(String name, MediaInfo mediaInfo) {
        super(name, mediaInfo);
        this.name = name;
        this.file = new File(mediaInfo.getFilePath());
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("file", file)
                .toString();
    }
}
