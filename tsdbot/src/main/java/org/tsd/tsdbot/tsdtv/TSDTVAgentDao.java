package org.tsd.tsdbot.tsdtv;

import io.dropwizard.hibernate.AbstractDAO;
import io.dropwizard.hibernate.UnitOfWork;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.SessionFactory;

import java.util.List;

@SuppressWarnings("unchecked")
public class TSDTVAgentDao extends AbstractDAO<TSDTVAgent> {

    public TSDTVAgentDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @UnitOfWork
    public List<TSDTVAgent> listAllAgents() {
        return currentSession().createQuery("FROM TSDTVAgent").getResultList();
    }

    @UnitOfWork
    public String saveAgent(TSDTVAgent agent) {
        if (agent.getId() == null) {
            persist(agent);
        } else {
            currentSession().merge(agent);
        }
        return agent.getId();

    }

    @UnitOfWork
    public TSDTVAgent getAgentByAgentId(String agentId) {
        List result = query("FROM TSDTVAgent WHERE agentId = :id")
                .setParameter("id", agentId)
                .list();
        return CollectionUtils.isEmpty(result) ? null : (TSDTVAgent) result.get(0);
    }
}
