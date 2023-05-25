package org.tsd.tsdbot.tsdtv;

import io.dropwizard.hibernate.AbstractDAO;
import io.dropwizard.hibernate.UnitOfWork;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class TSDTVEpisodicItemDao extends AbstractDAO<TSDTVEpisodicItem> {

    private static final Logger log = LoggerFactory.getLogger(TSDTVEpisodicItemDao.class);

    public TSDTVEpisodicItemDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @UnitOfWork
    public TSDTVEpisodicItem getCurrentEpisode(String seriesName, String seasonName) {
        log.info("Getting current episode, series={}, season={}", seriesName, seasonName);
        StringBuilder builder = new StringBuilder("FROM TSDTVEpisodicItem WHERE seriesName = :series");
        if (StringUtils.isNotBlank(seasonName)) {
            builder.append(" AND seasonName = :season");
        }

        org.hibernate.query.Query<TSDTVEpisodicItem> query = query(builder.toString());
        query.setParameter("series", seriesName);
        if (StringUtils.isNotBlank(seasonName)) {
            query.setParameter("season", seasonName);
        }

        TSDTVEpisodicItem item = uniqueResult(query);

        if (item == null) {
            log.info("No episode info in database for series={} and season={}, initializing...",
                    seriesName, seasonName);
            item = new TSDTVEpisodicItem();
            item.setSeriesName(seriesName);
            item.setSeasonName(seasonName);
            item.setCurrentEpisode(1);
            persist(item);
        }

        log.info("Returning episodic info: {}", item);
        return item;
    }

    @UnitOfWork
    public void setCurrentEpisode(String seriesName, String seasonName, int episode) {
        log.info("Setting current episode, seriesName={}, seasonName={}, episode={}",
                seriesName, seasonName, episode);

        StringBuilder updateStatement = new StringBuilder("UPDATE TSDTVEpisodicItem SET currentEpisode = :episode " +
                "WHERE seriesName = :series");

        if (StringUtils.isNotBlank(seasonName)) {
            updateStatement.append(" AND seasonName = :season");
        } else {
            updateStatement.append(" AND seasonName IS NULL");
        }

        Query query = currentSession()
                .createQuery(updateStatement.toString())
                .setParameter("episode", episode)
                .setParameter("seriesName", seriesName);

        if (StringUtils.isNotBlank(seasonName)) {
            query.setParameter("seasonName", seasonName);
        }

        int updatedCount = query.executeUpdate();
        log.info("Set episodic info, updated = {}", updatedCount);
    }
}
