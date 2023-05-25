package org.tsd.tsdtv;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.app.module.FfmpegModule;
import org.tsd.app.module.UtilityModule;
import org.tsd.tsdtv.module.TorrentModule;
import org.tsd.tsdtv.release.ReleaseFetcherThread;
import org.tsd.tsdtv.release.ReleaseSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TSDTVAgentApplication extends Application<TSDTVAgentConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(TSDTVAgentApplication.class);

    public static void main(final String[] args) throws Exception {
        new TSDTVAgentApplication().run(args);
    }

    @Override
    public String getName() {
        return "TSDTV Agent";
    }

    public void run(final TSDTVAgentConfiguration tsdtvAgentConfiguration, Environment environment) throws Exception {

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {

                install(new UtilityModule());
                install(new TorrentModule(tsdtvAgentConfiguration));

                log.info("Binding agentId: {}", tsdtvAgentConfiguration.getAgentId());
                bind(String.class)
                        .annotatedWith(Names.named("agentId"))
                        .toInstance(tsdtvAgentConfiguration.getAgentId());

                URL tsdbotUrl;
                try {
                    tsdbotUrl = new URL(tsdtvAgentConfiguration.getTsdbotUrl());
                    log.info("Binding tsdbotUrl: {}", tsdbotUrl);
                    bind(URL.class)
                            .annotatedWith(Names.named("tsdbotUrl"))
                            .toInstance(tsdbotUrl);
                } catch (Exception e) {
                    throw new RuntimeException("Invalid TSDBot URL: " + tsdtvAgentConfiguration.getTsdbotUrl(), e);
                }

                install(new FfmpegModule(tsdtvAgentConfiguration.getFfmpeg()));

                log.info("Binding password: {}", tsdtvAgentConfiguration.getPassword());
                bind(String.class)
                        .annotatedWith(Names.named("password"))
                        .toInstance(tsdtvAgentConfiguration.getPassword());

                File inventoryDirectory;
                try {
                    inventoryDirectory = new File(tsdtvAgentConfiguration.getInventoryPath());
                    log.info("Binding inventory directory: {}", inventoryDirectory);
                    if (!inventoryDirectory.exists()) {
                        throw new IOException("TSDTV inventory folder does not exist: " + tsdtvAgentConfiguration.getInventoryPath());
                    }
                    bind(File.class)
                            .annotatedWith(Names.named("inventory"))
                            .toInstance(inventoryDirectory);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to initialize TSDTV inventory", e);
                }

                HttpClient httpClient = HttpClients.createDefault();
                TSDBotClient tsdBotClient = new TSDBotClient(httpClient,
                        tsdbotUrl,
                        tsdtvAgentConfiguration.getAgentId(),
                        tsdtvAgentConfiguration.getPassword(),
                        new ObjectMapper());
                log.info("Built TSDBot client: {}", tsdBotClient);
                bind(TSDBotClient.class)
                        .toInstance(tsdBotClient);

                bind(new TypeLiteral<Map<ReleaseSource, List<String>>>() {})
                        .annotatedWith(Names.named("monitoringReleases"))
                        .toInstance(tsdtvAgentConfiguration.getReleases());

                bind(TSDTVPlayer.class);
            }
        });

        Stream.of(injector.getInstance(NetworkMonitor.class),
                injector.getInstance(HeartbeatThread.class),
                injector.getInstance(JobPollingThread.class),
                injector.getInstance(ReleaseFetcherThread.class))
                .map(runnable -> new Thread(runnable, runnable.getClass()+"-ServiceThread"))
                .peek(thread -> log.warn("Starting thread: {}", thread.getName()))
                .forEach(Thread::start);
    }
}
