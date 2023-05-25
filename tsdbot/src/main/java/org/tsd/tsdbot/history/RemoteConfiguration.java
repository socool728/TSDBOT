package org.tsd.tsdbot.history;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class RemoteConfiguration implements Serializable {
    private IgnorableMessageInfo ignorableMessageInfo;
    private List<String> blacklistedUsers = new LinkedList<>();

    public IgnorableMessageInfo getIgnorableMessageInfo() {
        return ignorableMessageInfo;
    }

    public void setIgnorableMessageInfo(IgnorableMessageInfo ignorableMessageInfo) {
        this.ignorableMessageInfo = ignorableMessageInfo;
    }

    public List<String> getBlacklistedUsers() {
        return blacklistedUsers;
    }

    public void setBlacklistedUsers(List<String> blacklistedUsers) {
        this.blacklistedUsers = blacklistedUsers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ignorableMessageInfo", ignorableMessageInfo)
                .append("blacklistedUsers", blacklistedUsers)
                .toString();
    }
}
