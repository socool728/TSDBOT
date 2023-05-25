package org.tsd.tsdbot.auth;

import io.dropwizard.hibernate.AbstractDAO;
import io.dropwizard.hibernate.UnitOfWork;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UserDao extends AbstractDAO<User> {

    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    public UserDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @UnitOfWork
    public User findUserByUsername(String username) {
        List result = currentSession()
                .createQuery("FROM User WHERE username = :u")
                .setParameter("u", username)
                .setMaxResults(1)
                .getResultList();
        return CollectionUtils.isEmpty(result) ? null : (User) result.get(0);
    }

    @UnitOfWork
    public User findUserById(String id) {
        return currentSession().find(User.class, id);
    }
}
