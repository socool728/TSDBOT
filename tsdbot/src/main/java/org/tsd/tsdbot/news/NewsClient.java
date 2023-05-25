package org.tsd.tsdbot.news;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class NewsClient {

    private static final Logger log = LoggerFactory.getLogger(NewsClient.class);

    private final Clock clock;
    private final HttpClient httpClient;
    private final String newsApiKey;

    @Inject
    public NewsClient(Clock clock,
                      HttpClient httpClient,
                      @Named(Constants.Annotations.NEWS_API_KEY) String newsApiKey) {
        this.clock = clock;
        this.httpClient = httpClient;
        this.newsApiKey = newsApiKey;
    }

    public NewsQueryResult queryForNews(String topic) throws IOException, URISyntaxException {
        return queryForNews(topic,
                Language.en,
                LocalDateTime.now(clock).minus(1, ChronoUnit.DAYS),
                SortBy.popularity);
    }

    public NewsQueryResult queryForNews(String topic,
                                        Language language,
                                        LocalDateTime fromDate,
                                        SortBy sortBy) throws URISyntaxException, IOException {
        String languageString = language == null ? Language.en.name() : language.name();

        if (fromDate == null) {
            fromDate = LocalDateTime.now(clock).minus(1, ChronoUnit.DAYS);
        }

        String fromDateString = fromDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("America/Chicago")));

        String sortByString = sortBy == null ? SortBy.popularity.name() : sortBy.name();

        URI uri = new URIBuilder("https://newsapi.org/v2/everything")
                .addParameter("q", String.format("\"%s\"", topic))
                .addParameter("language", languageString)
                .addParameter("from", fromDateString)
                .addParameter("sortBy", sortByString)
                .addParameter("apiKey", newsApiKey)
                .build();

        HttpGet get = new HttpGet(uri);
        ObjectMapper objectMapper = new ObjectMapper();
        try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(get)) {
            NewsQueryResult newsQueryResult = objectMapper.readValue(response.getEntity().getContent(), NewsQueryResult.class);
            log.info("News query result: \"{}\" -> {}", newsQueryResult);
            return newsQueryResult;
        }
    }
}
