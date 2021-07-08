package utils;

import utils.tuples.UserTuple;

public class ClientDataSingleton {

    private static ClientDataSingleton instance;
    private ClientDataSingleton() { }

    public String userName = "";
    public String userNick = "";
    public boolean initialOnlineStatus;
    public int initialLatitude = 0;
    public int initialLongitude = 0;
    public int receivePort;

    public int detectionRadius = 10;

    private String brokerIP = "";

    public static ClientDataSingleton getInstance() {
        if (instance == null) {
            instance = new ClientDataSingleton();
        }
        return instance;
    }

    public void saveUserDataFromTuple(UserTuple user) {
        this.userName = user.userNick;
    }
}
