package org.tsd.tsdbot.view;

import io.dropwizard.views.View;
import org.tsd.tsdbot.auth.User;

import java.nio.charset.Charset;

public abstract class TSDHQView extends View {

    protected final User loggedInUser;

    public TSDHQView(String templateName) {
        this(templateName, null);
    }

    public TSDHQView(String templateName, User user) {
        super(templateName, Charset.forName("UTF-8"));
        this.loggedInUser = user;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }
}
