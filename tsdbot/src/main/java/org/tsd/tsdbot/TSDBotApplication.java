package org.tsd.tsdbot;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import de.btobastian.javacord.DiscordAPI;
import de.btobastian.javacord.Javacord;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.PolymorphicAuthDynamicFeature;
import io.dropwizard.auth.PolymorphicAuthValueFactoryProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.app.module.UtilityModule;
import org.tsd.tsdbot.app.config.TSDBotConfiguration;
import org.tsd.tsdbot.app.module.*;
import org.tsd.tsdbot.async.ChannelThreadFactory;
import org.tsd.tsdbot.auth.*;
import org.tsd.tsdbot.discord.DiscordChannel;
import org.tsd.tsdbot.discord.DiscordUser;
import org.tsd.tsdbot.filename.FilenameLibrary;
import org.tsd.tsdbot.filename.S3FilenameLibrary;
import org.tsd.tsdbot.history.HistoryCache;
import org.tsd.tsdbot.history.RemoteConfigurationRepository;
import org.tsd.tsdbot.history.filter.FilterFactory;
import org.tsd.tsdbot.listener.CreateMessageListener;
import org.tsd.tsdbot.listener.MessageFilter;
import org.tsd.tsdbot.listener.MessageHandler;
import org.tsd.tsdbot.listener.channel.*;
import org.tsd.tsdbot.listener.user.ConfigReloadHandler;
import org.tsd.tsdbot.meme.MemeRepository;
import org.tsd.tsdbot.meme.S3MemeRepository;
import org.tsd.tsdbot.odb.OdbItem;
import org.tsd.tsdbot.printout.PrintoutLibrary;
import org.tsd.tsdbot.resources.*;
import org.tsd.tsdbot.tsdtv.AgentRegistry;
import org.tsd.tsdbot.tsdtv.TSDTV;
import org.tsd.tsdbot.tsdtv.TSDTVAgent;
import org.tsd.tsdbot.tsdtv.TSDTVEpisodicItem;
import org.tsd.tsdbot.tsdtv.library.TSDTVLibrary;
import org.tsd.tsdbot.tsdtv.quartz.TSDTVJobFactory;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TSDBotApplication extends Application<TSDBotConfiguration> {

    private static final Logger log = LoggerFactory.getLogger(TSDBotApplication.class);

    private final HibernateBundle<TSDBotConfiguration> hibernate = new HibernateBundle<TSDBotConfiguration>(
            User.class,
            OdbItem.class,
            TSDTVAgent.class,
            TSDTVEpisodicItem.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(TSDBotConfiguration configuration) {
            return configuration.getDatabase();
        }
    };

    public static void main(final String[] args) throws Exception {
        new TSDBotApplication().run(args);
    }

    @Override
    public String getName() {
        return "TSDBot";
    }

    @Override
    public void initialize(final Bootstrap<TSDBotConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(final TSDBotConfiguration configuration,
                    final Environment environment) {

        DiscordAPI api = Javacord.getApi(configuration.getBotToken(), true);
        api.connectBlocking();
        log.info("Connected!");

        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                install(new UtilityModule());
                install(new DiscordModule(api, configuration));
                install(new HibernateModule(hibernate));
                install(new TSDBotModule(configuration));
                install(new TSDTVModule(configuration.getFfmpeg(), configuration.getTsdtv()));

                bind(Twitter.class)
                        .toInstance(TwitterFactory.getSingleton());

                install(new S3Module(configuration));

                bind(FilenameLibrary.class)
                        .to(S3FilenameLibrary.class);

                bind(MemeRepository.class)
                        .to(S3MemeRepository.class);

                bind(PrintoutLibrary.class);
                bind(AgentRegistry.class);
                bind(TSDTVLibrary.class);
                bind(RemoteConfigurationRepository.class);

                install(new FactoryModuleBuilder().build(ChannelThreadFactory.class));
                install(new FactoryModuleBuilder().build(FilterFactory.class));
            }
        });

        configureQuartz(injector);

        HistoryCache historyCache = injector.getInstance(HistoryCache.class);

        CreateMessageListener messageListener = injector.getInstance(CreateMessageListener.class);
        messageListener.addFilter(historyCache);

        List<MessageHandler<DiscordChannel>> channelMessageHandlers = Arrays.asList(
                injector.getInstance(ReplaceHandler.class),
                injector.getInstance(ChooseHandler.class),
                injector.getInstance(DeejHandler.class),
                injector.getInstance(DorjHandler.class),
                injector.getInstance(FilenameHandler.class),
                injector.getInstance(GvHandler.class),
                injector.getInstance(HustleHandler.class),
                injector.getInstance(PrintoutHandler.class),
                injector.getInstance(OmniDatabaseHandler.class),
                injector.getInstance(TSDTVHandler.class),
                injector.getInstance(MorningHandler.class),
                injector.getInstance(NewsHandler.class),
                injector.getInstance(RecapHandler.class),
                injector.getInstance(BlacklistHandler.class),
                injector.getInstance(MemeHandler.class));

        List<MessageHandler<DiscordUser>> userMessageHandlers = Arrays.asList(
                injector.getInstance(ConfigReloadHandler.class));

//        List<MessageFilter> messageFilters = Arrays.asList(
//                injector.getInstance(HustleFilter.class));
        List<MessageFilter> messageFilters = new LinkedList<>();

        for (MessageHandler<DiscordChannel> channelMessageHandler : channelMessageHandlers) {
            messageListener.addChannelHandler(channelMessageHandler);
            historyCache.addChannelMessageHandler(channelMessageHandler);
        }

        for (MessageHandler<DiscordUser> userMessageHandler : userMessageHandlers) {
            messageListener.addUserHandler(userMessageHandler);
            historyCache.addUserMessageHandler(userMessageHandler);
        }

        for (MessageFilter messageFilter : messageFilters) {
            messageListener.addFilter(messageFilter);
            historyCache.addMessageFilter(messageFilter);
        }

        api.registerListener(messageListener);
        historyCache.initialize();

        /*
        Configure authentication
         */
        TokenAuthFilter tokenAuthFilter = new TokenAuthFilter.Builder()
                .setAuthenticator(injector.getInstance(TokenAuthenticator.class))
                .setAuthorizer(new UserAuthorizer())
                .setRealm(Constants.Auth.USER_REALM)
                .setUnauthorizedHandler((prefix, realm) -> Response.seeOther(UriBuilder.fromUri("/login").build()).build())
                .buildAuthFilter();

        ServiceAuthFilter serviceAuthFilter = injector.getInstance(ServiceAuthFilter.Builder.class)
                .setAuthenticator(injector.getInstance(ServiceAuthenticator.class))
                .setRealm(Constants.Auth.SERVICE_REALM)
                .buildAuthFilter();

        final PolymorphicAuthDynamicFeature authDynamicFeature = new PolymorphicAuthDynamicFeature<>(
                ImmutableMap.of(
                        TSDTVAgent.class, serviceAuthFilter,
                        User.class, tokenAuthFilter));

        final AbstractBinder binder = new PolymorphicAuthValueFactoryProvider.Binder<>(
                ImmutableSet.of(TSDTVAgent.class, User.class));

        environment.jersey().register(authDynamicFeature);
        environment.jersey().register(binder);
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        environment.getObjectMapper().registerModules(
                new ParameterNamesModule(),
                new Jdk8Module(),
                new JavaTimeModule());

        environment.jersey().register(injector.getInstance(SplashResource.class));
        environment.jersey().register(injector.getInstance(LoginResource.class));
        environment.jersey().register(injector.getInstance(LogoutResource.class));
        environment.jersey().register(injector.getInstance(FilenameResource.class));
        environment.jersey().register(injector.getInstance(RandomFilenameResource.class));
        environment.jersey().register(injector.getInstance(HustleResource.class));
        environment.jersey().register(injector.getInstance(PrintoutResource.class));
        environment.jersey().register(injector.getInstance(TSDTVResource.class));
        environment.jersey().register(injector.getInstance(JobResource.class));
        environment.jersey().register(injector.getInstance(DashboardResource.class));
        environment.jersey().register(injector.getInstance(TSDTVAgentResource.class));
        environment.jersey().register(injector.getInstance(LoggingResource.class));
        environment.jersey().register(injector.getInstance(TSDTVReleaseResource.class));
        environment.jersey().register(injector.getInstance(MemeResource.class));
    }

    private static void configureQuartz(Injector injector) {
        try {
            Scheduler scheduler = injector.getInstance(Scheduler.class);
            TSDTV tsdtv = injector.getInstance(TSDTV.class);
            scheduler.setJobFactory(new TSDTVJobFactory(tsdtv));
        } catch (Exception e) {
            System.err.println("Failed to configure TSDTV quartz scheduler");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
