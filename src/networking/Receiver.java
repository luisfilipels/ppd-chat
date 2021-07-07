package networking;

import javafx.application.Platform;
import main.MainViewController;
import utils.ClientDataSingleton;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Receiver implements Runnable{

    // Reference to the socket created by NetworkHandlerSingleton
    DatagramSocket socket;

    Receiver(DatagramSocket socket) {
        this.socket = socket;
    }

    void handleChat(String sender, String message) {
        if (sender.equals(ClientDataSingleton.getInstance().userID)) return;
        System.out.println(sender + ":" + message);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                MainViewController.logMessage(sender + ": " + message);
            }
        });
    }

    void handlePing(String author) {
        // TODO: Check lecture for unequal radius conditions
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                MainViewController.addContact(author);
            }
        });
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1000]; // Cria um buffer local
        try {
            DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);

            while (true) {
                socket.receive(pacote);
                System.out.println("Packet received");
                String contents = new String(pacote.getData(), 0, pacote.getLength());

                String[] parts = contents.split("\\|");

                if (parts[0].equals("chat")) {
                    handleChat(parts[1], parts[2]);
                } else if (parts[0].equals("ping")) {
                    System.out.println("Received ping from " + parts[1]);
                    handlePing(parts[1]);
                } else {
                    System.out.println("Message with invalid header received! Header: " + parts[0]);
                }

                pacote.setLength(buffer.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
