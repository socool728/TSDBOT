package org.tsd.tsdtv;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.rest.v1.tsdtv.Heartbeat;
import org.tsd.rest.v1.tsdtv.HeartbeatResponse;
import org.tsd.rest.v1.tsdtv.NewReleaseNotification;
import org.tsd.rest.v1.tsdtv.StoppedPlayingNotification;
import org.tsd.rest.v1.tsdtv.job.Job;
import org.tsd.rest.v1.tsdtv.job.JobResult;
import org.tsd.tsdtv.release.Release;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class TSDBotClient {

    private static final Logger log = LoggerFactory.getLogger(TSDBotClient.class);

    private static final int MAX_ATTEMPTS = 10;
    private static final long ATTEMPT_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(2);

    private final HttpClient httpClient;
    private final URL tsdbotUrl;
    private final ObjectMapper objectMapper;
    private final String agentId;
    private final String serviceAuthPassword;

    public TSDBotClient(HttpClient httpClient,
                        URL tsdbotUrl,
                        String agentId,
                        String serviceAuthPassword,
                        ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.tsdbotUrl = tsdbotUrl;
        this.agentId = agentId;
        this.serviceAuthPassword = serviceAuthPassword;
        this.objectMapper = objectMapper;
        log.info("Initialized TSDBotClient with URL {}", tsdbotUrl);
    }

    public HeartbeatResponse sendTsdtvAgentHeartbeat(Heartbeat heartbeat) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(tsdbotUrl.toURI())
                .setPath("/tsdtv/agent/"+heartbeat.getAgentId());
        URI uri = uriBuilder.build();
        log.info("Sending TSDTV agent heartbeat, URI={}", uri);

        HttpPut put = new HttpPut(uri);
        applyAuthHeader(put);
        applyJsonEntity(put, heartbeat);
        
        try (CloseableHttpResponse response = getResponseWithRedundancy(httpClient, put)) {
            if (response.getStatusLine().getStatusCode()/100 != 2) {
                String msg = String.format("HTTP error %d: %s",
                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                throw new Exception(msg);
            }
            String responseString = EntityUtils.toString(response.getEntity());
            log.debug("Heartbeat successful, received response: {}", responseString);
            return objectMapper.readValue(responseString, HeartbeatResponse.class);
        }
    }

    public void sendMediaStoppedNotification(int mediaId, boolean error) throws Exception {
        StoppedPlayingNotification notification = new StoppedPlayingNotification();
        notification.setAgentId(agentId);
        notification.setMediaId(mediaId);
        notification.setError(error);

        URIBuilder uriBuilder = new URIBuilder(tsdbotUrl.toURI())
                .setPath("/tsdtv/stopped");
        URI uri = uriBuilder.build();
        log.info("Sending TSDTV agent stopped notification, URI={}, entity={}", uri, notification);

        HttpPost post = new HttpPost(uri);
        applyAuthHeader(post);
        applyJsonEntity(post, notification);

        try (CloseableHttpResponse response = getResponseWithRedundancy(httpClient, post)) {
            if (response.getStatusLine().getStatusCode()/100 != 2) {
                String msg = String.format("HTTP error %d: %s",
                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                throw new Exception(msg);
            }
        }
    }

    public Job pollForJob() throws Exception {
        URIBuilder uriBuilder = new URIBuilder(tsdbotUrl.toURI())
                .setPath("/job/"+agentId);
        URI uri = uriBuilder.build();
        log.debug("Sending job poll request, URI={}", uri);

        HttpGet get = new HttpGet(uri);
        applyAuthHeader(get);

        try (CloseableHttpResponse response = getResponseWithRedundancy(httpClient, get)) {
            if (response.getStatusLine().getStatusCode()/100 != 2) {
                String msg = String.format("HTTP error %d: %s",
                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                throw new Exception(msg);
            }
            if (response.getStatusLine().getStatusCode() == Response.Status.OK.getStatusCode()) {
                String responseString = EntityUtils.toString(response.getEntity());
                Job job = objectMapper.readValue(responseString, Job.class);
                log.debug("Received job from server: {}", job);
                return job;
            }
            return null;
        }
    }

    public void sendJobResult(JobResult result) {
        try {
            URIBuilder uriBuilder = new URIBuilder(tsdbotUrl.toURI())
                    .setPath("/job/" + result.getJobId());
            URI uri = uriBuilder.build();
            log.debug("Sending job poll request, URI={}, result={}", uri, result);

            HttpPut put = new HttpPut(uri);
            applyAuthHeader(put);
            applyJsonEntity(put, result);

            try (CloseableHttpResponse response = getResponseWithRedundancy(httpClient, put)) {
                if (response.getStatusLine().getStatusCode() / 100 != 2) {
                    String msg = String.format("HTTP error %d: %s",
                            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                    throw new Exception(msg);
                }
                String responseString = EntityUtils.toString(response.getEntity());
                log.debug("Job update successful, received response: {}", responseString);
            }
        } catch (Exception e) {
            log.error("Error sending job result: " + result, e);
        }
    }

    public void notifyNewRelease(Release release) {
        try {
            URIBuilder uriBuilder = new URIBuilder(tsdbotUrl.toURI())
                    .setPath("/release");
            URI uri = uriBuilder.build();
            log.debug("Sending new release notification, URI={}, release={}", uri, release);

            NewReleaseNotification notification = new NewReleaseNotification();
            notification.setSeries(release.getSeriesName());
            notification.setEpisodeName(release.getEpisodeName());
            notification.setEpisodeNumber(release.getEpisodeNumber());

            HttpPut put = new HttpPut(uri);
            applyAuthHeader(put);
            applyJsonEntity(put, notification);

            try (CloseableHttpResponse response = getResponseWithRedundancy(httpClient, put)) {
                if (response.getStatusLine().getStatusCode() / 100 != 2) {
                    String msg = String.format("HTTP error %d: %s",
                            response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
                    throw new Exception(msg);
                }
                String responseString = EntityUtils.toString(response.getEntity());
                log.debug("New release notification successful, received response: {}", responseString);
            }
        } catch (Exception e) {
            log.error("Error sending new notification release: " + release, e);
        }
    }

    private void applyAuthHeader(HttpUriRequest request) {
        request.addHeader(Constants.Auth.SERVICE_AUTH_TOKEN_HEADER, serviceAuthPassword);
        request.addHeader(Constants.Auth.SERVICE_AUTH_NAME_HEADER, agentId);
    }

    private void applyJsonEntity(HttpEntityEnclosingRequest request, Object entity) throws JsonProcessingException {
        String entityString = objectMapper.writeValueAsString(entity);
        request.setEntity(new StringEntity(entityString, ContentType.APPLICATION_JSON));
        request.setHeader("Content-Type", MediaType.APPLICATION_JSON);
    }

    private CloseableHttpResponse getResponseWithRedundancy(HttpClient client, HttpUriRequest request) throws Exception {
        int attempts = 0;
        Exception error = null;
        while (attempts < MAX_ATTEMPTS) {
            try {
                return (CloseableHttpResponse) client.execute(request);
            } catch (Exception e) {
                log.error("Error during execution, retrying after " + ATTEMPT_INTERVAL_MILLIS + " ms");
                log.error("Error", e);
                try {
                    error = e;
                    attempts++;
                    Thread.sleep(ATTEMPT_INTERVAL_MILLIS);
                } catch (InterruptedException ie) {
                    log.error("Interrupted");
                }
            }
        }

        throw error;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("tsdbotUrl", tsdbotUrl)
                .append("agentId", agentId)
                .append("serviceAuthPassword", serviceAuthPassword)
                .toString();
    }
}
