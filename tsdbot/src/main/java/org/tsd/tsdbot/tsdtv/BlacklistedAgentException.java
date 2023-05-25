package org.tsd.tsdbot.tsdtv;

public class BlacklistedAgentException extends TSDTVException {
    public BlacklistedAgentException(String agentId) {
        super(agentId+" is blacklisted");
    }
}
