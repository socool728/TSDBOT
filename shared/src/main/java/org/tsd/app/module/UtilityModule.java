package org.tsd.app.module;

import com.google.inject.AbstractModule;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import java.time.Clock;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UtilityModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new QuartzModule());

        HttpClient httpClient = HttpClients.createDefault();
        bind(HttpClient.class)
                .toInstance(httpClient);

        ExecutorService executorService
                = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        bind(ExecutorService.class)
                .toInstance(executorService);

        bind(Clock.class)
                .toInstance(Clock.systemUTC());

        bind(Random.class)
                .toInstance(new Random());
    }
}
