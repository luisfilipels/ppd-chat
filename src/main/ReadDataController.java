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
import net.jini.core.transaction.TransactionException;
import networking.NetworkHandlerSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;

import java.rmi.RemoteException;

public class ReadDataController {

    @FXML
    private TextField userField;

    @FXML
    private Text failedConnectionText;

    private NetworkHandlerSingleton networkHandler;

    @FXML
    public void initialize() {
        failedConnectionText.setOpacity(0);
        networkHandler = NetworkHandlerSingleton.getInstance();
    }

    @FXML
    public void onConfirmButton(ActionEvent event) {
        String userName = userField.getText().trim();

        if (!loginWithUserName(userName)) return;
        if (!connectToRoom()) return;

        closeSelfWindow(event);
        openMainWindowWithUserName(userName);
    }

    private boolean connectToRoom() {
        try {
            if (!networkHandler.auctionTrackerExists()) {
                System.out.println("Auction tracker offline. Creating...");
                networkHandler.writeAuctionTracker();
            }
        } catch (AcquireTupleException e) {
            failedConnectionText.setText("Couldn't connect to auction space!");
            failedConnectionText.setOpacity(1);
            return false;
        } catch (WriteTupleException e) {
            failedConnectionText.setText("Couldn't write data in auction space!");
            failedConnectionText.setOpacity(1);
            return false;
        }
        return true;
    }

    private boolean loginWithUserName(String userName) {
        try {
            networkHandler.loginUser(userName);
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
