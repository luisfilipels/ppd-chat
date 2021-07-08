package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.ContactEntryHBox;
import utils.exceptions.AcquireTupleException;

import javax.jms.JMSException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;

public class MainViewController {

    @FXML
    private ListView<String> chatListView;
    private static ObservableList<String> chatList;

    @FXML
    private ListView<ContactEntryHBox> contactsListView;
    private static ObservableList<ContactEntryHBox> contactsList;
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
        int countSelected = 0;
        for (ContactEntryHBox contact : contactsList) {
            if (contact.selectedCheckbox.isSelected()) countSelected++;
        }
        for (ContactEntryHBox contact : contactsList) {
            if (!contact.selectedCheckbox.isSelected() && countSelected > 0) continue;
            try {
                networkHandler.sendChatMessage(contact.userNick, message);
            } catch (AcquireTupleException | JMSException | IllegalAccessException e) {
                System.out.println("Couldn't send message to " + contact + "!");
                e.printStackTrace();
            }
        }
        logMessage("(Você): " + message);
        messageField.clear();
    }

    @FXML
    void initialize() {
        clientData = ClientDataSingleton.getInstance();
        try {
            networkHandler = NetworkHandlerSingleton.getInstance();
        } catch (Exception e) {
            System.out.println("Could not get network handler!");
            e.printStackTrace();
            System.exit(0);
        }
        setUpLists();
        setUpErrors();
        updateContactList();
        setUpReadings();
    }

    public static void logMessage(String message) {
        chatList.add(message);
    }

    public void updateContactList() {
        List<String> contactsToAdd = networkHandler.getNeighborhood();

        for (String userID : contactsToAdd) {
            addContact(userID);
        }
    }

    public static void addContact(String contactNick) {
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
        if (contactsAdded.contains(contactNick) || contactNick.equals(clientData.userNick)) return;

        try {
            System.out.println("Pinging user " + contactNick);
            NetworkHandlerSingleton.getInstance().pingUser(contactNick);
        } catch (AcquireTupleException e) {
            System.out.println("Couldn't ping user " + contactNick + "!");
            e.printStackTrace();
        }

        contactsAdded.add(contactNick);
        String contactName = NetworkHandlerSingleton.getInstance().getUserName(contactNick);

        Text text = new Text(contactName + " (" + contactNick + ")");
        Region spacer = new Region();
        CheckBox checkBox = new CheckBox("Send message?");
        Button deleteButton = new Button("Excluir");

        deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                contactsAdded.remove(contactNick);
                ContactEntryHBox boxToBeRemoved = null;
                for (ContactEntryHBox hbox : contactsList) {
                    if (hbox.userNick.equals(contactNick)) boxToBeRemoved = hbox;
                }
                contactsList.remove(boxToBeRemoved);
            }
        });
        ContactEntryHBox hBox = new ContactEntryHBox(contactNick, contactName, checkBox);
        hBox.getChildren().addAll(text, spacer, checkBox, deleteButton);
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
    }

    private void setUpReadings() {
        setNewRadiusText(clientData.detectionRadius);
        setNewLatitudeText(clientData.initialLatitude);
        setNewLongitudeText(clientData.initialLongitude);
        setOnlineStatusText(clientData.initialOnlineStatus);
        isOnlineCheckBox.setSelected(clientData.initialOnlineStatus);
    }

    @FXML
    void onRadiusAction() {
        Integer radius = getValueFromField(contactRadiusField);
        if (radius == null || radius < 0) {
            showErrorOnField(contactRadiusField);
            return;
        }
        clientData.detectionRadius = radius;
        updateContactList();
        setNewRadiusText(radius);
        getQueuedMessagesIfOnline(networkHandler.amOnline());
        contactRadiusField.clear();
    }

    @FXML
    void onLatitudeFieldAction() {
        Integer latitude = getValueFromField(latitudeField);
        if (latitude == null) {
            showErrorOnField(latitudeField);
            return;
        }
        if (!updateMyUser(null, latitude, null)) return;
        updateContactList();
        setNewLatitudeText(latitude);
        latitudeField.clear();
    }

    @FXML
    void onLongitudeFieldAction() {
        Integer longitude = getValueFromField(longitudeField);
        if (longitude == null) {
            showErrorOnField(longitudeField);
            return;
        }
        if (!updateMyUser(longitude, null, null)) return;
        updateContactList();
        setNewLongitudeText(longitude);
        longitudeField.clear();
    }

    Integer getValueFromField(TextField field) {
        int value = -1;
        try {
            value = Integer.parseInt(field.getText());
        } catch (NumberFormatException ex) {
            showErrorOnField(field);
            return null;
        }
        return value;
    }

    void showErrorOnField(TextField field) {
        field.setText("Valor inválido!");
        field.setDisable(true);
        Timer refreshTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                field.setDisable(false);
                field.clear();
            }
        });
        refreshTimer.setRepeats(false);
        refreshTimer.start();
    }

    @FXML
    void onOnlineStatusChanged() {
        boolean isOnline = isOnlineCheckBox.isSelected();
        if (!updateMyUser(null, null, isOnline)) return;
        updateContactList();
        setOnlineStatusText(isOnline);
    }

    private boolean updateMyUser(Integer longitude, Integer latitude, Boolean isOnline) {
        try {
            networkHandler.updateMyUser(latitude, longitude, isOnline);
            getQueuedMessagesIfOnline(isOnlineCheckBox.isSelected());
        } catch (Exception e) {
            // TODO: Add UI error messages
            e.printStackTrace();
            return false;
        }
        return true;
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

    private void getQueuedMessagesIfOnline(boolean isOnline) {
        if (isOnline) {
            try {
                List<String> list = networkHandler.getConsumerMessages();
                for (String s : list) {
                    String[] messageParts = s.split("\\|");
                    String userNick = messageParts[1];
                    String message = messageParts[2];
                    String userName = networkHandler.getUserName(userNick);
                    logMessage(userName + "(" + userNick + "):" + message);
                }
            } catch (JMSException | IllegalAccessException | NullPointerException e) {
                System.out.println("Couldn't get queue messages!");
                e.printStackTrace();
            }
        }
    }

}
