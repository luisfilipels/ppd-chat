package networking;

import utils.ClientDataSingleton;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Semaphore;

public class Sender implements Runnable{

    // Used to avoid polling the stringToSend field for new data to send to the other player's client.
    // When there is no new message to send, Sender's thread sleeps, and is awakened when a new message arrives
    // from other objects in the game.
    Semaphore s = new Semaphore(0);

    // Reference to the socket created by NetworkHandlerSingleton
    DatagramSocket socket;

    Sender(DatagramSocket socket) {
        this.socket = socket;
    }

    private String stringToSend;
    private String address;
    private int port;

    public void setStringToSend(String string, String address, int port) {
        this.stringToSend = string;
        this.address = address;
        this.port = port;
        s.release();
        // Releases the mutex so the thread can continue to run
    }

    private void waitForData() {
        try {
            s.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            InetAddress remote = InetAddress.getByName("localhost");

            while (true) {
                waitForData();

                System.out.println("Sending data to address " + address);

                String message = "chat|" + ClientDataSingleton.getInstance().userID + "|" + stringToSend;
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, remote, 1025);
                socket.send(packet);
            }
            /*InetAddress server = InetAddress.getByName(SessionDataSingleton.getInstance().getRemoteAddress());
            int port = SessionDataSingleton.getInstance().getSendPort();

            while (true) {
                String message = getStringToSend();
                byte[] data = message.getBytes();
                DatagramPacket pacote = new DatagramPacket(data, data.length, server, port);
                socket.send(pacote);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
