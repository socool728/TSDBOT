package org.tsd.rest.v1.tsdtv;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedList;
import java.util.List;

public class Inventory {

    private List<Movie> movies = new LinkedList<>();
    private List<Series> series = new LinkedList<>();

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("movies", movies)
                .append("series", series)
                .toString();
    }
}
