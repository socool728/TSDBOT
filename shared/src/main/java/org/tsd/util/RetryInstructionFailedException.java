package org.tsd.util;

public class RetryInstructionFailedException extends Exception {
    public RetryInstructionFailedException(String name, int attempts) {
        super("Failed to successfully execute instructions \""+name+"\" after "+attempts+" attempts");
    }
}
