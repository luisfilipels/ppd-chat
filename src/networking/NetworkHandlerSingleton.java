package networking;

import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;

import javax.jms.JMSException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkHandlerSingleton {

    private static NetworkHandlerSingleton instance;
    private final TupleSpaceManager manager;
    private Sender sender;
    private Receiver receiver;
    private DatagramSocket socket;
    private Producer producer;
    private Consumer consumer;

    private Thread t1;
    private Thread t2;

    private NetworkHandlerSingleton() throws SocketException {
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
        manager.addSelfToTrackerIfUserExists(getMyIP());
    }

    public void sendChatMessage(String to, String message) throws AcquireTupleException, JMSException, IllegalAccessException {
        if (!manager.userIsReachable(to)) {
            producer.produceMessage(to, "chat|" + ClientDataSingleton.getInstance().userID + "|" + message);
            return;
        }

        String userAddress = manager.getUserIP(to);

        String ip = userAddress.split("\\|")[0];
        String port = userAddress.split("\\|")[1];

        message = "chat|" + ClientDataSingleton.getInstance().userID + "|" + message;

        sender.setStringToSend(message, ip, Integer.parseInt(port));
    }

    private void sendPingMessage(String to, String from) throws AcquireTupleException {
        if (!manager.userIsReachable(to)) {
            // TODO: Do asynchronous communication here
            return;
        }

        String userAddress = manager.getUserIP(to);

        String ip = userAddress.split("\\|")[0];
        String port = userAddress.split("\\|")[1];

        String message = "ping|" + ClientDataSingleton.getInstance().userID;

        sender.setStringToSend(message, ip, Integer.parseInt(port));
    }

    public List<String> getNeighborhood() {
        try {
            return manager.getNeighborhood();
        } catch (AcquireTupleException e) {
            return Collections.emptyList();
        }
    }

    public String getMyIP() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress("google.com", 80));
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder(socket.getLocalAddress().toString());
        sb.deleteCharAt(0);
        return sb.toString();
    }

    public void updateMyUser(int latitude, int longitude, boolean isOnline) throws AcquireTupleException, WriteTupleException {
        manager.updateUserProperties(latitude, longitude, isOnline);
    }

    public void startSocket() throws SocketException {
        socket = new DatagramSocket(ClientDataSingleton.getInstance().receivePort);
        sender = new Sender(socket);
        receiver = new Receiver(socket);

        t1 = new Thread(sender);
        t2 = new Thread(receiver);

        t1.start();
        t2.start();
    }

    public void startProducerAndConsumer() throws JMSException {
        producer = new Producer();
        consumer = new Consumer();

        producer.startProducer();
        consumer.startConsumer();
    }

    public List<String> getConsumerMessages() throws JMSException, IllegalAccessException {
        List<String> returnList = new ArrayList<>();
        while (consumer.hasMessages()) {
            returnList.add(consumer.consumeMessage());
        }
        return returnList;
    }

    public void pingUser(String contact) throws AcquireTupleException {
        if (!manager.userIsReachable(contact)) {
            // TODO: Do asynchronous communication here
            return;
        }
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
        sendPingMessage(contact, clientData.userID);
    }

    public void setMyselfToOffline() {
        try {
            manager.setMyselfToOffline();
        } catch (AcquireTupleException | WriteTupleException e) {
            System.out.println("Could not set self to offline!");
            e.printStackTrace();
        }
    }
}
