package org.tsd.tsdbot.app.module;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.tsd.Constants;
import org.tsd.tsdbot.app.config.TSDBotConfiguration;

public class S3Module extends AbstractModule {

    private final TSDBotConfiguration configuration;

    public S3Module(TSDBotConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(
                        configuration.getAws().getAccessKey(),
                        configuration.getAws().getSecretKey()));

        AmazonS3 s3Client = AmazonS3Client.builder()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.DEFAULT_REGION)
                .build();
        bind(AmazonS3.class)
                .toInstance(s3Client);

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.S3_FILENAMES_BUCKET))
                .toInstance(configuration.getAws().getFilenamesBucket());

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.S3_MEMES_BUCKET))
                .toInstance(configuration.getAws().getMemesBucket());

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.S3_RANDOM_FILENAMES_BUCKET))
                .toInstance(configuration.getAws().getRandomFilenamesBucket());

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.S3_TSDTV_BUCKET))
                .toInstance(configuration.getAws().getTsdtvBucket());

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.S3_TSDTV_IMAGES_BUCKET))
                .toInstance(configuration.getAws().getTsdtvQueueImagesBucket());

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.S3_TSDTV_COMMERCIALS_BUCKET))
                .toInstance(configuration.getAws().getTsdtvCommercialsBucket());

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.S3_TSDBOT_CONFIG_BUCKET))
                .toInstance(configuration.getAws().getConfigBucket());
    }
}
