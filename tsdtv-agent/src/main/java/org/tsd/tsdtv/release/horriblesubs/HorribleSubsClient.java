package org.tsd.tsdtv.release.horriblesubs;

import com.google.inject.Inject;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class HorribleSubsClient {

    private static final Logger log = LoggerFactory.getLogger(HorribleSubsClient.class);

    private static final String SD_480_FEED = "http://horriblesubs.info/rss.php?res=sd";

    private final HttpClient httpClient;

    @Inject
    public HorribleSubsClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public List<HorribleSubsRelease> getReleases() throws IOException, FeedException {
        try (CloseableHttpResponse response = (CloseableHttpResponse)httpClient.execute(new HttpGet(SD_480_FEED))) {
            InputStream inputStream = response.getEntity().getContent();
            SyndFeedInput feedInput = new SyndFeedInput();
            SyndFeed feed = feedInput.build(new XmlReader(inputStream));
            return feed.getEntries()
                    .stream()
                    .map(HorribleSubsRelease::new)
                    .peek(release -> log.info("Found HorribleSubs release: {}", release))
                    .collect(Collectors.toList());
        }
    }
}
