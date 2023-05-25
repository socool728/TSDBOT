package org.tsd.tsdbot.history;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.discord.DiscordMessage;

import java.io.IOException;
import java.io.Serializable;

@Singleton
public class RemoteConfigurationRepository implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(RemoteConfigurationRepository.class);

    private static final String REMOTE_CONFIG_FILE = "remoteConfig.json";

    private RemoteConfiguration remoteConfiguration;

    private final AmazonS3 s3Client;
    private final String tsdbotConfigBucket;
    private final ObjectMapper objectMapper;

    @Inject
    public RemoteConfigurationRepository(AmazonS3 s3Client,
                                         @Named(Constants.Annotations.S3_TSDBOT_CONFIG_BUCKET) String tsdbotConfigBucket) {
        this.s3Client = s3Client;
        this.tsdbotConfigBucket = tsdbotConfigBucket;
        this.objectMapper = new ObjectMapper();

        try {
            load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load TSDBot config from AWS, bucket="+tsdbotConfigBucket, e);
        }
    }

    public void load() throws IOException {
        log.warn("Loading remote config info, tsdbotConfigBucket={}, file={}",
                tsdbotConfigBucket, REMOTE_CONFIG_FILE);

        synchronized (this) {
            S3Object object = s3Client.getObject(tsdbotConfigBucket, REMOTE_CONFIG_FILE);
            this.remoteConfiguration = objectMapper.readValue(object.getObjectContent(), RemoteConfiguration.class);
            log.info("Retrieved remote config: {}", remoteConfiguration);
        }
    }

    public void upload() throws JsonProcessingException {
        String configString = objectMapper.writeValueAsString(remoteConfiguration);
        log.warn("Uploading remote config info, tsdbotConfigBucket={}, file={}, data=\n{}",
                tsdbotConfigBucket, REMOTE_CONFIG_FILE, remoteConfiguration);

        synchronized (this) {
            PutObjectResult result
                    = s3Client.putObject(tsdbotConfigBucket, REMOTE_CONFIG_FILE, configString);
            log.info("Uploaded remote config: {}", result);
        }
    }

    public RemoteConfiguration getRemoteConfiguration() {
        return remoteConfiguration;
    }

    public boolean isMessageInIgnorablePattern(DiscordMessage message) {
        String text = message.getContent();
        return remoteConfiguration.getIgnorableMessageInfo().getPatterns().stream().anyMatch(text::matches);
    }

    public boolean isMessageFromIgnorableUser(DiscordMessage message) {
        String author = message.getAuthor().getName();
        return remoteConfiguration.getIgnorableMessageInfo().getUsers()
                .stream()
                .anyMatch(ignorableUser -> StringUtils.equalsIgnoreCase(ignorableUser, author));
    }

    public boolean isMessageFromBlacklistedUser(DiscordMessage message) {
        String author = message.getAuthor().getName();
        return remoteConfiguration.getBlacklistedUsers().stream().anyMatch(user -> StringUtils.equalsIgnoreCase(user, author));
    }
}
