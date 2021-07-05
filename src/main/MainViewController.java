package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;
import utils.tuples.AuctionTrackerTuple;
import utils.tuples.UserTuple;

public class MainViewController {

    @FXML
    private ListView<HBox> chatListView;
    private ObservableList<HBox> chatList;

    @FXML
    private ListView<HBox> contactsListView;
    private ObservableList<HBox> contactsList;

    @FXML
    private TextField contactRadiusField;

    @FXML
    private Text chatViewErrorText;

    @FXML
    private Text contactListErrorText;

    @FXML
    private Text contactRadiusErrorText;
    
    @FXML
    private Text changeDataErrorText;

    private NetworkHandlerSingleton networkHandler;
    private ClientDataSingleton clientData;

    @FXML
    void initialize() {
        clientData = ClientDataSingleton.getInstance();
        try {
            networkHandler = NetworkHandlerSingleton.getInstance();
        } catch (Exception e) {
            System.out.println("Could not prepare JavaSpace for auctions!");
            e.printStackTrace();
            System.exit(0);
        }
        setUpLists();
        setUpErrors();
    }

    private void setUpErrors() {
        contactRadiusErrorText.setOpacity(0);
        chatViewErrorText.setOpacity(0);
        contactListErrorText.setOpacity(0);
        changeDataErrorText.setOpacity(0);
    }

    private void setUpLists() {
        chatList = FXCollections.observableArrayList();
        chatListView.setItems(chatList);

        contactsList = FXCollections.observableArrayList();
        contactsListView.setItems(contactsList);
    }
    
}
