package org.tsd.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class RetryUtil {

    private static final Logger log = LoggerFactory.getLogger(RetryUtil.class);

    // 1 second
    private static final long DEFAULT_PERIOD_MILLIS = TimeUnit.SECONDS.toMillis(1);

    public static void executeWithRetry(String name, int maxAttempts, RetryInstruction instructions)
            throws RetryInstructionFailedException {
        executeWithRetry(name, maxAttempts, DEFAULT_PERIOD_MILLIS, instructions);
    }

    public static void executeWithRetry(String name, int maxAttempts, long periodMillis, RetryInstruction instructions)
            throws RetryInstructionFailedException {

        if (maxAttempts <= 0 || periodMillis < 0) {
            throw new IllegalArgumentException("maxAttempts and period must be greater than 0: "
                    + maxAttempts + " / " + periodMillis);
        }

        boolean success = false;
        int attempts = 0;
        while (attempts < maxAttempts && !success) {
            try {
                attempts++;
                log.info("Executing with retry: {} (attempt {} of {}) (period = {}ms)",
                        name, attempts, maxAttempts, periodMillis);
                instructions.run();
                log.info("Successfully executed instructions: {}", name);
                success = true;
            } catch (Exception e) {
                log.error("Error executing instruction " + name, e);
                try {
                    Thread.sleep(periodMillis);
                } catch (Exception interrupt) {
                    log.error("Interrupted");
                }
            }
        }

        if (!success) {
            throw new RetryInstructionFailedException(name, attempts);
        }
    }

    public interface RetryInstruction {
        void run() throws Exception;
    }
}
