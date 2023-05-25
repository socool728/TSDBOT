package org.tsd.tsdbot.util;

import de.btobastian.javacord.entities.Server;
import de.btobastian.javacord.entities.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageSanitizerTest {

    private MessageSanitizer messageSanitizer;

    @Before
    public void setup() {
        Server server = mock(Server.class);

        User user = mock(User.class);
        when(user.getName()).thenReturn("Some Dude");

        when(server.getMemberById("0123456"))
                .thenReturn(user);

        this.messageSanitizer = new MessageSanitizer(server);
    }

    @Test
    public void testSanitizeMessage() {
        String sanitized = messageSanitizer.sanitize("<@0123456>, here is some text <:emoji:13489031234> https://www.youtube.com/yes/?huhhh=3.jpg :smirk: boy howdy");
        assertThat(sanitized, is("Some Dude, here is some text boy howdy"));
    }
}
