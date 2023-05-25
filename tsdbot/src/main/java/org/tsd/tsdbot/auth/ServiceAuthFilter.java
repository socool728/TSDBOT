package org.tsd.tsdbot.auth;

import io.dropwizard.auth.AuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;
import org.tsd.tsdbot.tsdtv.TSDTVAgent;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;
import java.io.IOException;

@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class ServiceAuthFilter extends AuthFilter<AgentCredentials, TSDTVAgent> {

    private static final Logger log = LoggerFactory.getLogger(ServiceAuthFilter.class);

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String tokenHeader = containerRequestContext.getHeaders().getFirst(Constants.Auth.SERVICE_AUTH_TOKEN_HEADER);
        String serviceNameHeader = containerRequestContext.getHeaders().getFirst(Constants.Auth.SERVICE_AUTH_NAME_HEADER);

        AgentCredentials credentials = new AgentCredentials();
        credentials.setAgentId(serviceNameHeader);
        credentials.setServiceAuthPassword(tokenHeader);

        if (!authenticate(containerRequestContext, credentials, "TSDHQService")) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(this.prefix, this.realm));
        }
    }

    public static class Builder extends AuthFilterBuilder<AgentCredentials, TSDTVAgent, ServiceAuthFilter> {
        protected ServiceAuthFilter newInstance() {
            return new ServiceAuthFilter();
        }
    }
}
