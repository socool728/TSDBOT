package org.tsd.tsdbot.auth;

public enum Role {
    peon    (0),
    staff   (1),
    admin   (2);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
