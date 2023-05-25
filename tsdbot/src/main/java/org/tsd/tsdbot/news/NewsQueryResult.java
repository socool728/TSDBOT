package org.tsd.tsdbot.news;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NewsQueryResult {

    @JsonProperty
    private String status;

    @JsonProperty
    private int totalResults;

    @JsonProperty
    private List<NewsArticle> articles;

    public List<NewsArticle> getArticles() {
        return articles;
    }

    public void setArticles(List<NewsArticle> articles) {
        this.articles = articles;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    @Override
    public String toString() {
        return "NewsQueryResult{" +
                "status='" + status + '\'' +
                ", totalResults=" + totalResults +
                ", articles=" + articles +
                '}';
    }
}
