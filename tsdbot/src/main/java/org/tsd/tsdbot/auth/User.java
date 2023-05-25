package org.tsd.tsdbot.auth;

import org.tsd.tsdbot.db.BaseEntity;

import javax.persistence.*;
import java.security.Principal;
import java.util.Date;

@Entity
public class User extends BaseEntity implements Principal {

    private String username;
    private String passwordHash;
    private String emailAddress;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoggedInTime;
    private String lastLoggedInFrom;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Date getLastLoggedInTime() {
        return lastLoggedInTime;
    }

    public void setLastLoggedInTime(Date lastLoggedInTime) {
        this.lastLoggedInTime = lastLoggedInTime;
    }

    public String getLastLoggedInFrom() {
        return lastLoggedInFrom;
    }

    public void setLastLoggedInFrom(String lastLoggedInFrom) {
        this.lastLoggedInFrom = lastLoggedInFrom;
    }

    @Override
    public String getName() {
        return username;
    }
}
