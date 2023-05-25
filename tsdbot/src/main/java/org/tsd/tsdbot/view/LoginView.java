package org.tsd.tsdbot.view;

import org.tsd.Constants;
import org.tsd.tsdbot.auth.User;

import java.util.Optional;

public class LoginView extends TSDHQView {

    public LoginView(Optional<User> user) {
        super(Constants.View.LOGIN_VIEW, user.orElse(null));
    }
}
