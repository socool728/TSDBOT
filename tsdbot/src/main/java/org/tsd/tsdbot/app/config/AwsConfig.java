package org.tsd.tsdbot.app.config;

import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class AwsConfig {

    @NotEmpty
    @NotNull
    private String accessKey;

    @NotEmpty
    @NotNull
    private String secretKey;

    @NotEmpty
    @NotNull
    private String filenamesBucket;

    @NotEmpty
    @NotNull
    private String memesBucket;

    @NotEmpty
    @NotNull
    private String randomFilenamesBucket;

    @NotEmpty
    @NotNull
    private String tsdtvBucket;

    @NotEmpty
    @NotNull
    private String tsdtvQueueImagesBucket;

    @NotEmpty
    @NotNull
    private String tsdtvCommercialsBucket;

    @NotEmpty
    @NotNull
    private String configBucket;

    public String getMemesBucket() {
        return memesBucket;
    }

    public void setMemesBucket(String memesBucket) {
        this.memesBucket = memesBucket;
    }

    public String getConfigBucket() {
        return configBucket;
    }

    public void setConfigBucket(String configBucket) {
        this.configBucket = configBucket;
    }

    public String getTsdtvCommercialsBucket() {
        return tsdtvCommercialsBucket;
    }

    public void setTsdtvCommercialsBucket(String tsdtvCommercialsBucket) {
        this.tsdtvCommercialsBucket = tsdtvCommercialsBucket;
    }

    public String getTsdtvBucket() {
        return tsdtvBucket;
    }

    public void setTsdtvBucket(String tsdtvBucket) {
        this.tsdtvBucket = tsdtvBucket;
    }

    public String getTsdtvQueueImagesBucket() {
        return tsdtvQueueImagesBucket;
    }

    public void setTsdtvQueueImagesBucket(String tsdtvQueueImagesBucket) {
        this.tsdtvQueueImagesBucket = tsdtvQueueImagesBucket;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getFilenamesBucket() {
        return filenamesBucket;
    }

    public void setFilenamesBucket(String filenamesBucket) {
        this.filenamesBucket = filenamesBucket;
    }

    public String getRandomFilenamesBucket() {
        return randomFilenamesBucket;
    }

    public void setRandomFilenamesBucket(String randomFilenamesBucket) {
        this.randomFilenamesBucket = randomFilenamesBucket;
    }
}
