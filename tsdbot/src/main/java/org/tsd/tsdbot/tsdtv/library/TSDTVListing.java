package org.tsd.tsdbot.tsdtv.library;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tsd.rest.v1.tsdtv.Movie;
import org.tsd.rest.v1.tsdtv.Series;

import java.util.LinkedList;
import java.util.List;

public class TSDTVListing {
    private List<AgentMedia<Movie>> allMovies = new LinkedList<>();
    private List<AgentMedia<Series>> allSeries = new LinkedList<>();

    public List<AgentMedia<Movie>> getAllMovies() {
        return allMovies;
    }

    public void setAllMovies(List<AgentMedia<Movie>> allMovies) {
        this.allMovies = allMovies;
    }

    public List<AgentMedia<Series>> getAllSeries() {
        return allSeries;
    }

    public void setAllSeries(List<AgentMedia<Series>> allSeries) {
        this.allSeries = allSeries;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("allMovies", allMovies)
                .append("allSeries", allSeries)
                .toString();
    }
}
