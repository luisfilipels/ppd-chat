package utils.tuples;

import net.jini.core.entry.Entry;

import java.util.ArrayList;
import java.util.List;

public class UserTuple implements Entry {
    public String userID;

    public List<String> madeAuctions;

    public UserTuple() {}

    public UserTuple(String userID) {
        this.userID = userID;
    }
}
