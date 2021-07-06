package networking;

import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class NetworkHandlerSingleton {

    private static NetworkHandlerSingleton instance;
    private final TupleSpaceManager manager;
    private final Sender sender;
    private final Receiver receiver;
    private final DatagramSocket socket;

    private Thread t1;
    private Thread t2;

    private NetworkHandlerSingleton() throws SocketException {
        socket = new DatagramSocket(1025);
        sender = new Sender(socket);
        receiver = new Receiver(socket);
        manager = new TupleSpaceManager();
    }

    public static NetworkHandlerSingleton getInstance() {
        if (instance == null) {
            try {
                instance = new NetworkHandlerSingleton();
            } catch (SocketException e) {
                e.printStackTrace();
            }
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

    public void updateMyUser(int latitude, int longitude, boolean isOnline) throws AcquireTupleException, WriteTupleException {
        manager.updateUserProperties(latitude, longitude, isOnline);
    }

    public void startSocket() {
        t1 = new Thread(sender);
        t2 = new Thread(receiver);

        t1.start();
        t2.start();
    }
}
