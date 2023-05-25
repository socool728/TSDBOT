package org.tsd.tsdbot.view;

import org.tsd.Constants;
import org.tsd.tsdbot.auth.User;

public class DashboardView extends TSDHQView {

    public DashboardView(User user) {
        super(Constants.View.DASHBOARD_VIEW, user);
    }
}
