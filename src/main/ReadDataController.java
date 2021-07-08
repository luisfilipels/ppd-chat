package main;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;

import javax.jms.JMSException;
import java.net.BindException;
import java.net.SocketException;

public class ReadDataController {

    @FXML
    private TextField nickField;

    @FXML
    private TextField userIDField;

    @FXML
    private TextField radiusField;

    @FXML
    private TextField latitudeField;

    @FXML
    private TextField longitudeField;

    @FXML
    private CheckBox onlineStatusBox;

    @FXML
    private Text failedConnectionText;

    private NetworkHandlerSingleton networkHandler;
    private ClientDataSingleton clientData;

    @FXML
    public void initialize() {
        failedConnectionText.setOpacity(0);
        networkHandler = NetworkHandlerSingleton.getInstance();
        clientData = ClientDataSingleton.getInstance();
    }

    @FXML
    public void onConfirmButton(ActionEvent event) {
        String userName = userIDField.getText().trim();

        // TODO: Add input validation for all of these fields
        clientData.initialLongitude = Integer.parseInt(longitudeField.getText());
        clientData.initialLatitude = Integer.parseInt(latitudeField.getText());
        clientData.detectionRadius = Integer.parseInt(radiusField.getText());
        clientData.userNick = userIDField.getText();
        clientData.userName = nickField.getText();

        clientData.initialOnlineStatus = onlineStatusBox.isSelected();

        System.out.println("Got data from input");

        if (!connectToUserSpace()) {
            System.out.println("Couldn't connect to user space!");
            return;
        }
        if (!loginUser()) {
            System.out.println("Couldn't login user!");
        }

        int tryPort = 1025;
        while (true) {
            clientData.receivePort = tryPort;
            try {
                networkHandler.startSocket();
                break;
            } catch (BindException e) {
                System.out.println("Socket in use! Trying another...");
                tryPort++;
            } catch (SocketException e) {
                System.out.println("Couldn't start sockets!");
                e.printStackTrace();
                System.exit(0);
            }
        }



        System.out.println("Adding self to tracker");
        networkHandler.addSelfToTracker();

        try {
            networkHandler.startProducerAndConsumer();
        } catch (JMSException e) {
            System.out.println("Couldn't initialize async message code!");
            e.printStackTrace();
        }

        closeSelfWindow(event);
        openMainWindowWithUserName(userName);
    }

    private boolean connectToUserSpace() {
        try {
            if (!networkHandler.userTrackerExists()) {
                System.out.println("User tracker offline. Creating...");
                networkHandler.writeUserTracker();
            }
        } catch (AcquireTupleException e) {
            failedConnectionText.setText("Couldn't connect to user space!");
            failedConnectionText.setOpacity(1);
            return false;
        } catch (WriteTupleException e) {
            failedConnectionText.setText("Couldn't write data in auction space!");
            failedConnectionText.setOpacity(1);
            return false;
        }
        return true;
    }

    private boolean loginUser() {
        try {
            networkHandler.loginUser();
        } catch (AcquireTupleException e) {
            failedConnectionText.setText("Failed to login/register!");
            failedConnectionText.setOpacity(1);
            return false;
        } catch (WriteTupleException e) {
            failedConnectionText.setText("Failed to write data!");
            failedConnectionText.setOpacity(1);
            return false;
        }
        failedConnectionText.setOpacity(0);
        return true;
    }

    private void closeSelfWindow(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private void openMainWindowWithUserName(String userName) {
        try {
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
            primaryStage.setTitle("Cliente (" + userName + ")");
            primaryStage.setScene(new Scene(root, 800, 500));
            primaryStage.setResizable(false);
            primaryStage.show();
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    networkHandler.setMyselfToOffline();
                    System.exit(0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
