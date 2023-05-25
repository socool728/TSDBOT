package org.tsd.tsdbot.app.config;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;
import org.tsd.app.config.FfmpegConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class TSDBotConfiguration extends Configuration {

    @NotEmpty
    private String botId;

    @NotEmpty
    private String botToken;

    @NotEmpty
    private String serverId;

    @NotEmpty
    @NotNull
    private String stage;

    @NotEmpty
    @NotNull
    private String owner;

    @NotEmpty
    @NotNull
    private String encryptionKey;

    @NotEmpty
    @NotNull
    private String serviceAuthPassword;

    @NotEmpty
    @NotNull
    private String botUrl;

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @Valid
    @NotNull
    private TwitterConfig twitter;

    @Valid
    @NotNull
    private BitlyConfig bitly;

    @Valid
    @NotNull
    private AwsConfig aws;

    @NotNull
    @NotEmpty
    private String mashapeApiKey;

    @Valid
    @NotNull
    private GoogleConfig google;

    @Valid
    @NotNull
    private FfmpegConfig ffmpeg;

    @Valid
    @NotNull
    private TSDTVConfig tsdtv;

    @NotNull
    @NotEmpty
    private String newsApiKey;

    public String getNewsApiKey() {
        return newsApiKey;
    }

    public void setNewsApiKey(String newsApiKey) {
        this.newsApiKey = newsApiKey;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getServiceAuthPassword() {
        return serviceAuthPassword;
    }

    public void setServiceAuthPassword(String serviceAuthPassword) {
        this.serviceAuthPassword = serviceAuthPassword;
    }

    public FfmpegConfig getFfmpeg() {
        return ffmpeg;
    }

    public void setFfmpeg(FfmpegConfig ffmpeg) {
        this.ffmpeg = ffmpeg;
    }

    public TSDTVConfig getTsdtv() {
        return tsdtv;
    }

    public void setTsdtv(TSDTVConfig tsdtv) {
        this.tsdtv = tsdtv;
    }

    public GoogleConfig getGoogle() {
        return google;
    }

    public void setGoogle(GoogleConfig google) {
        this.google = google;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getMashapeApiKey() {
        return mashapeApiKey;
    }

    public void setMashapeApiKey(String mashapeApiKey) {
        this.mashapeApiKey = mashapeApiKey;
    }

    public String getBotUrl() {
        return botUrl;
    }

    public void setBotUrl(String botUrl) {
        this.botUrl = botUrl;
    }

    public AwsConfig getAws() {
        return aws;
    }

    public void setAws(AwsConfig aws) {
        this.aws = aws;
    }

    public BitlyConfig getBitly() {
        return bitly;
    }

    public void setBitly(BitlyConfig bitly) {
        this.bitly = bitly;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getBotId() {
        return botId;
    }

    public void setBotId(String botId) {
        this.botId = botId;
    }

    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public DataSourceFactory getDatabase() {
        return database;
    }

    public void setDatabase(DataSourceFactory database) {
        this.database = database;
    }

    public TwitterConfig getTwitter() {
        return twitter;
    }

    public void setTwitter(TwitterConfig twitter) {
        this.twitter = twitter;
    }
}
