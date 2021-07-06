package networking;

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

    public void setStringToSend(String string, String address) {
        this.stringToSend = string;
        this.address = address;
        s.release();
        // Releases the mutex so the thread can continue to run
    }

    public void waitForData() {
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

                String message = stringToSend;
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, remote, 1026);
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
