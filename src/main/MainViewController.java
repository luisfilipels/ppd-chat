package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;

import java.util.HashSet;
import java.util.List;

public class MainViewController {

    @FXML
    private ListView<String> chatListView;
    private static ObservableList<String> chatList;

    @FXML
    private ListView<HBox> contactsListView;
    private static ObservableList<HBox> contactsList;
    private static HashSet<String> contactsAdded;

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

    @FXML
    private Text radiusText;

    @FXML
    private Text latitudeText;

    @FXML
    private Text longitudeText;

    @FXML
    private CheckBox isOnlineCheckBox;

    @FXML
    private TextField longitudeField;

    @FXML
    private TextField latitudeField;

    @FXML
    private TextField messageField;

    private NetworkHandlerSingleton networkHandler;
    private ClientDataSingleton clientData;

    @FXML
    void onUpdateContactsClick() {
        updateContactList();
    }

    @FXML
    void onSendMessage() {
        String message = messageField.getText();
        for (String contact : contactsAdded) {
            try {
                networkHandler.sendMessageTo(contact, message);
            } catch (AcquireTupleException e) {
                System.out.println("Couldn't send message to " + contact + "!");
                e.printStackTrace();
            }
        }
    }

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
        updateContactList();
        setUpReadings();
    }

    public static void logMessage(String message) {
        // TODO: Implement this
        chatList.add(message);
    }

    public void updateContactList() {
        List<String> contactsToAdd = networkHandler.getNeighborhood();

        for (String userID : contactsToAdd) {
            addContact(userID);
        }
    }

    public static void addContact(String contact) {
        if (contactsAdded.contains(contact)) return;
        contactsAdded.add(contact);

        HBox hBox = new HBox();
        Text text = new Text(contact);
        Region spacer = new Region();
        CheckBox checkBox = new CheckBox("Send message?");
        hBox.getChildren().addAll(text, spacer, checkBox);
        hBox.setHgrow(spacer, Priority.ALWAYS);

        contactsList.add(hBox);
    }

    private void setUpLists() {
        chatList = FXCollections.observableArrayList();
        chatListView.setItems(chatList);

        contactsList = FXCollections.observableArrayList();
        contactsListView.setItems(contactsList);
        contactsAdded = new HashSet<>();
    }

    private void setUpErrors() {
        contactRadiusErrorText.setOpacity(0);
        chatViewErrorText.setOpacity(0);
        contactListErrorText.setOpacity(0);
        changeDataErrorText.setOpacity(0);
    }

    private void setUpReadings() {
        setNewRadiusText(clientData.detectionRadius);
        setNewLatitudeText(clientData.initialLatitude);
        setNewLongitudeText(clientData.initialLongitude);
        setOnlineStatusText(clientData.initialOnlineStatus);
    }

    @FXML
    void onConfirmButtonClick() {
        // TODO: Add input validation
        // TODO: Make it possible to update just one parameter at a time
        int radius = Integer.parseInt(contactRadiusField.getText());
        int longitude = Integer.parseInt(longitudeField.getText());
        int latitude = Integer.parseInt(latitudeField.getText());
        boolean isOnline = isOnlineCheckBox.isSelected();

        clientData.detectionRadius = radius;

        try {
            networkHandler.updateMyUser(latitude, longitude, isOnline);
        } catch (Exception e) {
            // TODO: Add UI error messages
            e.printStackTrace();
            return;
        }

        setNewRadiusText(radius);
        setNewLongitudeText(longitude);
        setNewLatitudeText(latitude);
        setOnlineStatusText(isOnline);
    }

    private void setNewRadiusText(int newRadius) {
        radiusText.setText("Raio (atual: " + newRadius + ")");
    }

    private void setNewLatitudeText(int newLatitude) {
        latitudeText.setText("Latitude (atual: " + newLatitude + ")");
    }

    private void setNewLongitudeText(int newLongitude) {
        longitudeText.setText("Longitude (atual: " + newLongitude + ")");
    }

    private void setOnlineStatusText(boolean isOnline) {
        isOnlineCheckBox.setText("Online? (atual: " + (isOnline ? "sim" : "não") + ")");
    }

}
