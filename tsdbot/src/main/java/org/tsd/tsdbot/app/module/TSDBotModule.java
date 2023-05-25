package org.tsd.tsdbot.app.module;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import org.tsd.Constants;
import org.tsd.tsdbot.app.BotUrl;
import org.tsd.tsdbot.app.Stage;
import org.tsd.tsdbot.app.config.TSDBotConfiguration;
import org.tsd.tsdbot.tsdtv.job.JobFactory;

import java.net.URL;

public class TSDBotModule extends AbstractModule {

    private final TSDBotConfiguration configuration;

    public TSDBotModule(TSDBotConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(TSDBotConfiguration.class)
                .toInstance(configuration);

        String stageString = configuration.getStage();
        Stage stage = Stage.valueOf(stageString);
        bind(Stage.class)
                .toInstance(stage);

        try {
            URL botUrl = new URL(configuration.getBotUrl());
            bind(URL.class)
                    .annotatedWith(BotUrl.class)
                    .toInstance(botUrl);
        } catch (Exception e) {
            System.err.println("Error reading botUrl from config: " + configuration.getBotUrl());
            throw new RuntimeException(e);
        }

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.ENCRYPTION_KEY))
                .toInstance(configuration.getEncryptionKey());

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.SERVICE_AUTH_PASSWORD))
                .toInstance(configuration.getServiceAuthPassword());

        bindGoogle();
        bindBitly();

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.MASHAPE_API_KEY))
                .toInstance(configuration.getMashapeApiKey());

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.NEWS_API_KEY))
                .toInstance(configuration.getNewsApiKey());

        install(new FactoryModuleBuilder().build(JobFactory.class));
    }

    private void bindGoogle() {
        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.GOOGLE_GIS_CX))
                .toInstance(configuration.getGoogle().getGisCx());

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.GOOGLE_API_KEY))
                .toInstance(configuration.getGoogle().getApiKey());
    }

    private void bindBitly() {
        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.BITLY_USER))
                .toInstance(configuration.getBitly().getUser());

        bind(String.class)
                .annotatedWith(Names.named(Constants.Annotations.BITLY_API_KEY))
                .toInstance(configuration.getBitly().getApiKey());
    }
}
