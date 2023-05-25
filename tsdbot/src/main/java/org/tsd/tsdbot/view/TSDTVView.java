package org.tsd.tsdbot.view;

import org.tsd.Constants;
import org.tsd.tsdbot.auth.User;
import org.tsd.tsdbot.tsdtv.library.TSDTVLibrary;
import org.tsd.tsdbot.tsdtv.library.TSDTVListing;

public class TSDTVView extends TSDHQView {

    private final TSDTVLibrary library;
    private final PlayerType playerType;
    private final String streamUrl;

    public TSDTVView(TSDTVLibrary library, PlayerType playerType, User user) {
        super(Constants.View.TSDTV_VIEW, user);
        this.library = library;
        this.streamUrl = library.getStreamUrl();
        this.playerType = playerType;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public TSDTVListing getListings() {
        return library.getListings();
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public enum PlayerType {
        vlc,
        videojs;
    }
}
