package org.tsd.tsdbot.auth;

import java.io.Serializable;

public class AgentCredentials implements Serializable {
    private String agentId;
    private String serviceAuthPassword;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getServiceAuthPassword() {
        return serviceAuthPassword;
    }

    public void setServiceAuthPassword(String serviceAuthPassword) {
        this.serviceAuthPassword = serviceAuthPassword;
    }
}
