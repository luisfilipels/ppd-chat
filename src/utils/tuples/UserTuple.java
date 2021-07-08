package utils.tuples;

import net.jini.core.entry.Entry;

public class UserTuple implements Entry {
    public String userNick;
    public String userName;

    public Integer latitude;
    public Integer longitude;

    public Boolean isOnline;

    public UserTuple() {}

    public UserTuple(String userNick) {
        this.userNick = userNick;
    }

    public UserTuple(String userNick, String userName, int latitude, int longitude, boolean isOnline) {
        this.userNick = userNick;
        this.userName = userName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isOnline = isOnline;
    }

    @Override
    public String toString() {
        return "UserTuple{" +
                "userID='" + userNick + '\'' +
                ", userNick='" + userName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", isOnline=" + isOnline +
                '}';
    }
}
