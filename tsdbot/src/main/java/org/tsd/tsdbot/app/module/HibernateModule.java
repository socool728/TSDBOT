package org.tsd.tsdbot.app.module;

import com.google.inject.AbstractModule;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.hibernate.UnitOfWorkAwareProxyFactory;
import org.hibernate.SessionFactory;
import org.tsd.tsdbot.app.config.TSDBotConfiguration;
import org.tsd.tsdbot.auth.UserDao;
import org.tsd.tsdbot.news.NewsTopicDao;
import org.tsd.tsdbot.odb.OdbItemDao;
import org.tsd.tsdbot.tsdtv.TSDTVAgentDao;
import org.tsd.tsdbot.tsdtv.TSDTVEpisodicItemDao;

public class HibernateModule extends AbstractModule {

    private final HibernateBundle<TSDBotConfiguration> hibernate;

    public HibernateModule(HibernateBundle<TSDBotConfiguration> hibernate) {
        this.hibernate = hibernate;
    }

    @Override
    protected void configure() {
        UnitOfWorkAwareProxyFactory proxyFactory = new UnitOfWorkAwareProxyFactory(hibernate);

        UserDao userDao = proxyFactory
                .create(UserDao.class, SessionFactory.class, hibernate.getSessionFactory());
        bind(UserDao.class)
                .toInstance(userDao);

        OdbItemDao odbItemDao = proxyFactory
                .create(OdbItemDao.class, SessionFactory.class, hibernate.getSessionFactory());
        bind(OdbItemDao.class)
                .toInstance(odbItemDao);

        TSDTVAgentDao tsdtvAgentDao = proxyFactory
                .create(TSDTVAgentDao.class, SessionFactory.class, hibernate.getSessionFactory());
        bind(TSDTVAgentDao.class)
                .toInstance(tsdtvAgentDao);

        TSDTVEpisodicItemDao tsdtvEpisodicItemDao = proxyFactory
                .create(TSDTVEpisodicItemDao.class, SessionFactory.class, hibernate.getSessionFactory());
        bind(TSDTVEpisodicItemDao.class)
                .toInstance(tsdtvEpisodicItemDao);

        NewsTopicDao newsTopicDao = proxyFactory
                .create(NewsTopicDao.class, SessionFactory.class, hibernate.getSessionFactory());
        bind(NewsTopicDao.class)
                .toInstance(newsTopicDao);

        bind(SessionFactory.class)
                .toInstance(hibernate.getSessionFactory());
    }
}
