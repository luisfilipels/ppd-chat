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
import javax.swing.*;
import java.awt.event.ActionListener;
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
        if (!tryToGetNumericalFields()) return;
        clientData.initialOnlineStatus = onlineStatusBox.isSelected();
        System.out.println("Got data from input");

        if (!connectToUserSpace()) {
            System.out.println("Couldn't connect to user space!");
            return;
        }
        if (!loginUser()) {
            System.out.println("Couldn't login user!");
        }
        getPortToBeUsed();
        networkHandler.addSelfToTracker();
        startProducerAndConsumer();

        closeSelfWindow(event);
        openMainWindowWithUserName(userName);
    }

    private boolean tryToGetNumericalFields() {
        boolean longitudeFailed = acquireLongitude();
        boolean latitudeFailed = acquireLatitude();
        boolean radiusFailed = acquireRadius();

        if (longitudeFailed) showErrorOnField(longitudeField, "Valor inválido!");
        if (latitudeFailed) showErrorOnField(latitudeField, "Valor inválido!");
        if (radiusFailed) showErrorOnField(radiusField, "Valor inválido!");

        boolean userNickFailed = acquireUserNick();
        boolean userNameFailed = acquireUserName();

        return !longitudeFailed && !latitudeFailed && !radiusFailed
                && !userNickFailed && !userNameFailed;
    }

    private boolean acquireLongitude() {
        boolean longitudeFailed = false;
        try {
            clientData.initialLongitude = Integer.parseInt(longitudeField.getText());
        } catch (NumberFormatException e) {
            longitudeFailed = true;
        }
        return longitudeFailed;
    }

    private boolean acquireLatitude() {
        boolean latitudeFailed = false;
        try {
            clientData.initialLatitude = Integer.parseInt(latitudeField.getText());
        } catch (NumberFormatException e) {
            latitudeFailed = true;
        }
        return latitudeFailed;
    }

    private boolean acquireRadius() {
        boolean radiusFailed = false;
        try {
            clientData.detectionRadius = Integer.parseInt(radiusField.getText());
            if (clientData.detectionRadius < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            radiusFailed = true;
        }
        return radiusFailed;
    }

    private boolean acquireUserNick() {
        boolean userNickFailed = false;
        clientData.userNick = userIDField.getText().trim();
        if (clientData.userNick.isEmpty()) {
            showErrorOnField(userIDField, "Não pode estar vazio!");
            userNickFailed = true;
        }
        return userNickFailed;
    }

    private boolean acquireUserName() {
        boolean userNameFailed = false;
        clientData.userName = nickField.getText().trim();
        if (clientData.userName.isEmpty()) {
            showErrorOnField(nickField, "Não pode estar vazio!");
            userNameFailed = true;
        }
        return userNameFailed;
    }

    private void getPortToBeUsed() {
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
    }

    private void startProducerAndConsumer() {
        try {
            networkHandler.startProducerAndConsumer();
        } catch (JMSException e) {
            System.out.println("Couldn't initialize async message code!");
            e.printStackTrace();
            System.exit(0);
        }
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

    void showErrorOnField(TextField field, String message) {
        field.setText(message);
        showTemporaryErrorOnField(field);
    }

    private void showTemporaryErrorOnField(TextField field) {
        field.setDisable(true);
        Timer refreshTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                field.setDisable(false);
                field.clear();
            }
        });
        refreshTimer.setRepeats(false);
        refreshTimer.start();
    }

}
