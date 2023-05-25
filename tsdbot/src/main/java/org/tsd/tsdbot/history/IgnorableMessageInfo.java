package org.tsd.tsdbot.history;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class IgnorableMessageInfo implements Serializable {
    private List<String> users = new LinkedList<>();
    private List<String> patterns = new LinkedList<>();

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getPatterns() {
        return patterns;
    }

    public void setPatterns(List<String> patterns) {
        this.patterns = patterns;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("users", users)
                .append("patterns", patterns)
                .toString();
    }
}
