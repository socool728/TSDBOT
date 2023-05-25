package org.tsd.tsdbot.hustle;

import com.google.common.collect.EvictingQueue;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordMessage;
import org.tsd.tsdbot.discord.DiscordUser;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class Hustle {

    private static final Logger log = LoggerFactory.getLogger(Hustle.class);

    private static final long MAX_THROTTLE_PERIOD_SECONDS = TimeUnit.MINUTES.toSeconds(10);

    private final EvictingQueue<HustleDataPoint> hustleBuffer = EvictingQueue.create(50);
    private final ExecutorService executorService;
    private final HttpClient httpClient;
    private final String apiKey;
    private final DiscordUser owner;
    private final Clock clock;

    private LocalDateTime nextApiCallAttempt = null;
    private long throttleSeconds = 0;

    @Inject
    public Hustle(ExecutorService executorService,
                  HttpClient httpClient,
                  Clock clock,
                  @Named(Constants.Annotations.OWNER) DiscordUser owner,
                  @Named(Constants.Annotations.MASHAPE_API_KEY) String apiKey) {
        this.clock = clock;
        this.executorService = executorService;
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.owner = owner;
    }

    public List<HustleDataPoint> getDataPoints() {
        return new LinkedList<>(hustleBuffer);
    }

    public double getCurrentHhr() {
        return calculateCurrentHhr();
    }

    public void process(DiscordMessage<?> message) {
        if (shouldCalculate()) {
            executorService.submit(() -> {
                log.debug("Sending latest message for sentiment analysis: {}", message);
                String responseString = null;
                try {
                    HttpPost post = new HttpPost("https://community-sentiment.p.mashape.com/text/");
                    post.addHeader("X-Mashape-Key", apiKey);
                    post.addHeader("Content-Type", "application/x-www-form-urlencoded");

                    List<NameValuePair> params = new LinkedList<>();
                    params.add(new BasicNameValuePair("txt", message.getContent()));
                    post.setEntity(new UrlEncodedFormEntity(params));

                    try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(post)) {
                        responseString = EntityUtils.toString(response.getEntity());
                        JSONObject json = new JSONObject(responseString);
                        json.keys().forEachRemaining(key -> {
                            String keyString = (String) key;
                            if (StringUtils.equals(keyString, "result")) {
                                HustleDataPoint dataPoint = getDataPointFromKey(message.getContent(), json, keyString);
                                log.debug("Analysis result: {}, Confidence {} -> Score = {}",
                                        dataPoint.getSentiment(), dataPoint.getConfidence(), dataPoint.getScore());
                                hustleBuffer.add(dataPoint);
                                dataPoint.setNewHhr(calculateCurrentHhr());
                                log.debug("New HHR: {}", dataPoint.getNewHhr());
                            }
                        });
                    }

                    throttleSeconds = 1;
                    nextApiCallAttempt = null;

                } catch (Exception e) {
                    log.error("Error retrieving text sentiment, response=\"\n" + responseString + "\"", e);
//                    owner.sendMessage("Error calculating hustle quotient: `" + responseString + "`");
                    throttleSeconds = Math.min(MAX_THROTTLE_PERIOD_SECONDS, throttleSeconds*2);
                    nextApiCallAttempt = LocalDateTime.now(clock).plusSeconds(throttleSeconds);
                }
            });
        }
    }

    private boolean shouldCalculate() {
        return nextApiCallAttempt == null || LocalDateTime.now(clock).isAfter(nextApiCallAttempt);
    }

    private static HustleDataPoint getDataPointFromKey(String originalMessage, JSONObject json, String key) {
        double confidence = Double.parseDouble(json.getJSONObject(key).getString("confidence"));
        HustleSentiment sentiment = HustleSentiment.fromString(json.getJSONObject(key).getString("sentiment"));
        return new HustleDataPoint(originalMessage, sentiment, confidence);
    }

    private double calculateCurrentHhr() {
        double hustle = 1;
        double hate = 1;
        for(HustleDataPoint dataPoint : hustleBuffer) {
            switch (dataPoint.getSentiment()) {
                case Positive: hustle += dataPoint.getScore(); break;
                case Negative: hate += dataPoint.getScore(); break;
                case Neutral: {
                    hustle += dataPoint.getScore();
                    hate += dataPoint.getScore();
                    break;
                }
            }
        }
        return hustle/hate;
    }
}
