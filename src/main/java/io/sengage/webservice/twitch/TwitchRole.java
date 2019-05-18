package io.sengage.webservice.twitch;

public enum TwitchRole {
    BROADCASTER,
    MODERATOR,
    VIEWER,
    EXTERNAL
    ;

    public static TwitchRole from(String value) {
        for (TwitchRole role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid enum type: " + value);
    }

}
