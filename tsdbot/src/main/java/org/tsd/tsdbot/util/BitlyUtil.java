package org.tsd.tsdbot.util;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.rosaloves.bitlyj.Bitly;
import org.tsd.Constants;

public class BitlyUtil {

    private final String user;
    private final String apiKey;

    @Inject
    public BitlyUtil(@Named(Constants.Annotations.BITLY_USER) String user,
                     @Named(Constants.Annotations.BITLY_API_KEY) String apiKey) {
        this.user = user;
        this.apiKey = apiKey;
    }

    public String shortenUrl(String input) {
        return Bitly.as(user, apiKey)
                .call(Bitly.shorten(input))
                .getShortUrl();
    }
}
