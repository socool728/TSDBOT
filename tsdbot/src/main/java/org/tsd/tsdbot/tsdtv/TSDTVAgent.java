package org.tsd.tsdbot.tsdtv;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.tsd.tsdbot.db.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.security.Principal;

@Entity
public class TSDTVAgent extends BaseEntity implements Principal {

    private String agentId;

    @Enumerated(EnumType.STRING)
    private AgentStatus status;

    private String lastHeartbeatFrom;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public AgentStatus getStatus() {
        return status;
    }

    public void setStatus(AgentStatus status) {
        this.status = status;
    }

    public String getLastHeartbeatFrom() {
        return lastHeartbeatFrom;
    }

    public void setLastHeartbeatFrom(String lastHeartbeatFrom) {
        this.lastHeartbeatFrom = lastHeartbeatFrom;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("agentId", agentId)
                .append("status", status)
                .append("lastHeartbeatFrom", lastHeartbeatFrom)
                .toString();
    }

    @Override
    public String getName() {
        return agentId;
    }
}
