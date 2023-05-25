package org.tsd.tsdbot.meme;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.util.MiscUtils;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.tsd.Constants.Meme.HALL_OF_FAME_PREFIX;
import static org.tsd.Constants.Meme.MAX_AGE_DAYS;

@Singleton
public class S3MemeRepository implements MemeRepository {

    private static final Logger log = LoggerFactory.getLogger(S3MemeRepository.class);

    private final AmazonS3 s3Client;
    private final HttpClient httpClient;
    private final String memesBucket;
    private final Clock clock;

    @Inject
    public S3MemeRepository(AmazonS3 s3Client,
                            HttpClient httpClient,
                            Clock clock,
                            @Named(Constants.Annotations.S3_MEMES_BUCKET) String memesBucket) {
        this.s3Client = s3Client;
        this.memesBucket = memesBucket;
        this.httpClient = httpClient;
        this.clock = clock;

        new Thread(new OldMemeReaper()).start();
    }

    @Override
    public boolean doesMemeExist(String id) {
        log.info("Checking if meme exists: {}/{}", memesBucket, id);
        return s3Client.doesObjectExist(memesBucket, id);
    }

    @Override
    public List<String> searchMemes(String partialId) {
        log.info("Searching for memes matching text: {}", partialId);
        return s3Client.listObjectsV2(memesBucket).getObjectSummaries()
                .stream()
                .filter(s3ObjectSummary -> StringUtils.containsIgnoreCase(s3ObjectSummary.getKey(), partialId))
                .map(S3ObjectSummary::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] getMeme(String id) throws IOException {
        log.info("Getting meme, id={}", id);

        if (!s3Client.doesObjectExist(memesBucket, id)) {
            throw new FileNotFoundException("Cannot find meme with ID "+id);
        }

        S3Object object = s3Client.getObject(memesBucket, id);
        log.info("Retrieved meme from S3, {} -> {}", id, object.toString());
        return IOUtils.toByteArray(object.getObjectContent());
    }

    @Override
    public String storeMeme(String memeUrl) throws IOException {
        log.info("Storing meme, url={}", memeUrl);

        HttpGet get = new HttpGet(memeUrl);
        byte[] responseBytes;
        try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(get)) {
            log.info("Meme response, {} -> {} \"{}\"",
                    memeUrl,
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase());

            if (response.getStatusLine().getStatusCode()/100 != 2) {
                throw new RuntimeException("Failed to download meme at URL "+memeUrl+": "+response.getStatusLine());
            }

            responseBytes = EntityUtils.toByteArray(response.getEntity());
        }

        String randomFilename = RandomStringUtils.randomAlphabetic(10)+".jpg";
        s3Client.putObject(memesBucket, randomFilename, new ByteArrayInputStream(responseBytes), new ObjectMetadata());
        log.info("Stored meme in S3, {} -> {}", memeUrl, randomFilename);

        return randomFilename;
    }

    @Override
    public String saveMeme(String partialId) throws IOException, MemeAlreadySavedException, MemeNotFoundException {
        log.info("Saving meme, partialId={}", partialId);

        List<String> match = searchMemes(partialId);

        if (CollectionUtils.isEmpty(match)) {
            throw new MemeNotFoundException("No meme found matching ID "+partialId);
        } else if (CollectionUtils.size(match) > 1) {
            throw new MemeNotFoundException("Multiple memes found matching ID "+partialId);
        }

        String oldKey = match.get(0);
        String newKey = HALL_OF_FAME_PREFIX+oldKey;

        if (doesMemeExist(newKey)) {
            throw new MemeAlreadySavedException(newKey);
        }

        byte[] data = getMeme(oldKey);
        s3Client.putObject(memesBucket, newKey, new ByteArrayInputStream(data), new ObjectMetadata());
        log.info("Saved meme in S3, {}", newKey);
        return newKey;
    }

    @Override
    public String getRandomSavedMeme() throws IOException {
        log.info("Retrieving random HOF meme");
        List<S3ObjectSummary> candidates = s3Client.listObjectsV2(memesBucket).getObjectSummaries()
                .stream()
                .filter(s3ObjectSummary -> StringUtils.startsWithIgnoreCase(s3ObjectSummary.getKey(), Constants.Meme.HALL_OF_FAME_PREFIX))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(candidates)) {
            return null;
        } else {
            return MiscUtils.getRandomItemInList(candidates).getKey();
        }
    }

    private class OldMemeReaper implements Runnable {

        private boolean shutdown = false;

        void shutdown() {
            this.shutdown = true;
        }

        @Override
        public void run() {
            log.info("Starting OldMemeReaper...");
            while (!shutdown) {
                Instant cutoff = Instant
                        .now(clock)
                        .minus(MAX_AGE_DAYS, ChronoUnit.DAYS);
                log.debug("Deleting memes older than {}", cutoff);

                List<S3ObjectSummary> memesToDelete = s3Client.listObjectsV2(memesBucket).getObjectSummaries()
                        .stream()
                        .filter(s3ObjectSummary -> !StringUtils.startsWithIgnoreCase(s3ObjectSummary.getKey(), Constants.Meme.HALL_OF_FAME_PREFIX))
                        .filter(s3ObjectSummary -> s3ObjectSummary.getLastModified().getTime() < cutoff.toEpochMilli())
                        .collect(Collectors.toList());

                for (S3ObjectSummary toDelete : memesToDelete) {
                    log.info("Deleting old meme ({}): {}", toDelete.getLastModified(), toDelete.getKey());
                    s3Client.deleteObject(memesBucket, toDelete.getKey());
                }

                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(10));
                } catch (InterruptedException e) {
                    log.error("Interrupted");
                    shutdown();
                }
            }
        }
    }
}
