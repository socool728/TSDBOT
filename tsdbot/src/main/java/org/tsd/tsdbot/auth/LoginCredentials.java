package org.tsd.tsdbot.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class LoginCredentials implements Serializable {
    @JsonProperty
    private String username;

    @JsonProperty
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
