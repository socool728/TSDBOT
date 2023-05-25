package org.tsd.tsdbot.tsdtv.job;

public class JobTimeoutException extends Exception {
    public JobTimeoutException() {
        super("Timed out waiting for agent to respond");
    }
}
