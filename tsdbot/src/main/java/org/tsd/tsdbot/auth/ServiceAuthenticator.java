package org.tsd.tsdbot.auth;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.tsdtv.TSDTVAgent;
import org.tsd.tsdbot.tsdtv.TSDTVAgentDao;

import java.util.Optional;

public class ServiceAuthenticator implements Authenticator<AgentCredentials, TSDTVAgent> {

    private static final Logger log = LoggerFactory.getLogger(ServiceAuthenticator.class);

    private final TSDTVAgentDao agentDao;
    private final String serviceAuthPassword;

    @Inject
    public ServiceAuthenticator(TSDTVAgentDao agentDao,
                                @Named(Constants.Annotations.SERVICE_AUTH_PASSWORD) String serviceAuthPassword) {
        this.agentDao = agentDao;
        this.serviceAuthPassword = serviceAuthPassword;
    }

    @Override
    public Optional<TSDTVAgent> authenticate(AgentCredentials credentials) throws AuthenticationException {
        if (!StringUtils.equals(credentials.getServiceAuthPassword(), serviceAuthPassword)) {
            return Optional.empty();
        }
        TSDTVAgent agent = agentDao.getAgentByAgentId(credentials.getAgentId());
        return Optional.ofNullable(agent);
    }
}
