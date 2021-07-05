package utils.tuples;

import net.jini.core.entry.Entry;

import java.util.ArrayList;
import java.util.List;

public class UserTuple implements Entry {
    public String userID;
    public String userNick;

    public Integer latitude;
    public Integer longitude;

    public Boolean isOnline;

    public UserTuple() {}

    public UserTuple(String userID) {
        this.userID = userID;
    }

    public UserTuple(String userID, String userNick, int latitude, int longitude, boolean isOnline) {
        this.userID = userID;
        this.userNick = userNick;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isOnline = isOnline;
    }
}
