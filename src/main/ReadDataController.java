package main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;

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

    @FXML
    private TextField listenPortField;

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
        clientData.userID = userIDField.getText();
        clientData.userNick = nickField.getText();
        clientData.receivePort = Integer.parseInt(listenPortField.getText());

        clientData.initialOnlineStatus = onlineStatusBox.isSelected();

        System.out.println("Got data from input");

        if (!connectToUserSpace()) return;
        if (!loginUser()) return;

        try {
            networkHandler.startSocket();
        } catch (SocketException e) {
            System.out.println("Couldn't start sockets!");
            e.printStackTrace();
        }

        System.out.println("Adding self to tracker");
        networkHandler.addSelfToTracker();

        closeSelfWindow(event);
        openMainWindowWithUserName(userName);
    }

    @FXML
    private void onSendMessage() {

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
