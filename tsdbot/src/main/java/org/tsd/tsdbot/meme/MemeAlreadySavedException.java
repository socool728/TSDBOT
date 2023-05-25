package org.tsd.tsdbot.meme;

public class MemeAlreadySavedException extends Exception {
    public MemeAlreadySavedException(String memeId) {
        super("Meme already saved: "+memeId);
    }
}
