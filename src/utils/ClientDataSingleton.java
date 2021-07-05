package utils;

import utils.tuples.UserTuple;

import java.util.ArrayList;
import java.util.List;

public class ClientDataSingleton {

    private static ClientDataSingleton instance;
    private ClientDataSingleton() { }

    public String userName = "";
    private String brokerIP = "";
    private String lastClickedID = "";

    public static ClientDataSingleton getInstance() {
        if (instance == null) {
            instance = new ClientDataSingleton();
        }
        return instance;
    }

    public void setLastClickedID(String id) {
        this.lastClickedID = id;
    }

    public String getLastClickedID() {
        return this.lastClickedID;
    }

    public void saveUserDataFromTuple(UserTuple user) {
        this.userName = user.userID;
    }

    public void setBrokerIP(String ip) {
        this.brokerIP = ip;
    }

    public String getBrokerIP(){
        return brokerIP;
    }

}
