package org.tsd.tsdbot.auth;

import io.dropwizard.auth.AuthFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tsd.Constants;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;
import java.io.IOException;

@PreMatching
@Priority(Priorities.AUTHENTICATION)
public class TokenAuthFilter extends AuthFilter<String, User> {

    private static final Logger log = LoggerFactory.getLogger(TokenAuthFilter.class);

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        String token = null;
        String header = containerRequestContext.getHeaders().getFirst(Constants.Auth.BASIC_AUTH);
        if (StringUtils.isNotBlank(header)) {
            String[] parts = header.split("\\s+");
            if(parts.length == 2 && parts[0].equals(Constants.Auth.BEARER)) {
                token = parts[1];
            }
        } else {
            if (containerRequestContext.getCookies().containsKey(Constants.Auth.TOKEN_KEY)) {
                token = containerRequestContext.getCookies().get(Constants.Auth.TOKEN_KEY).getValue();
            }
        }

        if (StringUtils.isBlank(token) || !authenticate(containerRequestContext, token, Constants.Auth.BEARER)) {
            throw new WebApplicationException(unauthorizedHandler.buildResponse(this.prefix, this.realm));
        }
    }

    public static class Builder extends AuthFilterBuilder<String, User, TokenAuthFilter> {
        public Builder() {
        }

        protected TokenAuthFilter newInstance() {
            return new TokenAuthFilter();
        }
    }
}
