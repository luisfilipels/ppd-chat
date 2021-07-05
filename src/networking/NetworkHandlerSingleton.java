package networking;

import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;

import java.util.Collections;
import java.util.List;

public class NetworkHandlerSingleton {

    private static NetworkHandlerSingleton instance;
    private final TupleSpaceManager manager;

    private NetworkHandlerSingleton() {
        manager = new TupleSpaceManager();
    }

    public static NetworkHandlerSingleton getInstance() {
        if (instance == null) {
            instance = new NetworkHandlerSingleton();
        }
        return instance;
    }

    public boolean userTrackerExists() throws AcquireTupleException {
        return manager.userTrackerExists();
    }

    public void writeUserTracker() throws WriteTupleException {
        manager.writeUserTracker();
    }

    public void loginUser() throws AcquireTupleException, WriteTupleException {
        manager.loginUser();
    }

    public void addSelfToTracker() {
        manager.addSelfToTrackerIfUserExists();
    }

    public List<String> getNeighborhood() {
        try {
            return manager.getNeighborhood();
        } catch (AcquireTupleException e) {
            return Collections.emptyList();
        }
    }
}
