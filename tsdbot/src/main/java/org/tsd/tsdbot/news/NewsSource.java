package org.tsd.tsdbot.news;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewsSource {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "NewsSource{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
