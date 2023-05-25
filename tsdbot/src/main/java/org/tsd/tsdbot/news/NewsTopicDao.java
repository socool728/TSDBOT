package org.tsd.tsdbot.news;

import io.dropwizard.hibernate.AbstractDAO;
import io.dropwizard.hibernate.UnitOfWork;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

@SuppressWarnings("unchecked")
public class NewsTopicDao extends AbstractDAO<String> {

    private static final Logger log = LoggerFactory.getLogger(NewsTopicDao.class);

    public NewsTopicDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @UnitOfWork
    public List<String> getTopicsForUser(String userId) {
        log.info("Getting news topics for user: {}", userId);
        return currentSession()
                .createNativeQuery("SELECT topic FROM NewsTopic WHERE userId = :id")
                .setParameter("id", userId)
                .getResultList();
    }

    @UnitOfWork
    public void addTopicForUser(String userId, String topic) {
        log.info("Adding topic for user {}: \"{}\"", userId, topic);
        BigInteger count = (BigInteger)currentSession()
                .createNativeQuery("SELECT COUNT(*) FROM NewsTopic WHERE userId = :id AND topic = :topic")
                .setParameter("id", userId)
                .setParameter("topic", topic)
                .getSingleResult();
        log.info("Existing count for user={}, topic=\"{}\"", userId, topic);
        if (count.longValue() == 0) {
            currentSession()
                    .createNativeQuery("INSERT INTO NewsTopic (userId, topic) VALUES (:userId, :topic)")
                    .setParameter("userId", userId)
                    .setParameter("topic", topic)
                    .executeUpdate();
            log.info("Successfully added topic for user {}: \"{}\"", userId, topic);
        }
    }

    @UnitOfWork
    public void removeTopicForUser(String userId, String topic) {
        log.info("Removing topic for user {}: \"{}\"", userId, topic);
        int affectedRows = currentSession()
                .createNativeQuery("DELETE FROM NewsTopic WHERE userId = :userId AND topic = :topic")
                .setParameter("userId", userId)
                .setParameter("topic", topic)
                .executeUpdate();
        log.info("Successfully removed topic for user {}: \"{}\" (affected rows: {})",
                userId, topic, affectedRows);
    }
}
