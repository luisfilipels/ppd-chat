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

    public void setStringToSend(String message, String address, int port) {
        this.stringToSend = message;
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
            while (true) {
                waitForData();

                InetAddress remote = InetAddress.getByName(address);
                System.out.println("Sending data to address " + address);

                byte[] data = stringToSend.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, remote, port);
                socket.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
