package org.tsd.tsdbot.view;

import org.tsd.Constants;
import org.tsd.tsdbot.auth.User;

public class TSDTVAgentsView extends TSDHQView {

    public TSDTVAgentsView(User user) {
        super(Constants.View.TSDTV_AGENTS_VIEW, user);
    }
}
