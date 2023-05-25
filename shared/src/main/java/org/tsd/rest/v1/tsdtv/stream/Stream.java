package org.tsd.rest.v1.tsdtv.stream;

import java.util.HashMap;
import java.util.Map;

public abstract class Stream {

    protected int index;
    protected String codecName;
    protected Map<String, String> tags = new HashMap<>();

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getCodecName() {
        return codecName;
    }

    public void setCodecName(String codecName) {
        this.codecName = codecName;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
