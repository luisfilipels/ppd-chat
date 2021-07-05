package utils.tuples;

import net.jini.core.entry.Entry;

import java.util.HashMap;

public class BatchTuple implements Entry {

    public String id;
    public String description;
    public String sellerId;
    public HashMap<String, String> bids = new HashMap<>();

    public BatchTuple(){}

    public BatchTuple(String id, String description, String sellerId) {
        this.id = id;
        this.description = description;
        this.sellerId = sellerId;
        bids = new HashMap<>();
    }

    public BatchTuple(String id, String description, String sellerId, HashMap<String, String> bids) {
        this.id = id;
        this.description = description;
        this.sellerId = sellerId;
        this.bids = bids;
    }

    public void addBid(String creator, int value, boolean isPublic) {
        String bidData = value + "|" + isPublic;
        System.out.println(bidData);
        bids.put(creator, bidData);
    }
}
