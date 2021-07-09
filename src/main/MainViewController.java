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
import utils.exceptions.WriteTupleException;

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
    private Text onlineStatusErrorText;

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
        String messageToSend = messageField.getText();
        int countSelected = getCountOfSelectedContacts();
        sendMessageToSelectedContacts(messageToSend, countSelected);
        logMessage("(Você): " + messageToSend);
        messageField.clear();
    }

    private int getCountOfSelectedContacts() {
        int countSelected = 0;
        for (ContactEntryHBox contact : contactsList) {
            if (contact.selectedCheckbox.isSelected()) countSelected++;
        }
        return countSelected;
    }

    private void sendMessageToSelectedContacts(String messageToSend, int countSelected) {
        for (ContactEntryHBox contact : contactsList) {
            // If there is at least one selected box, and the current contact has not
            // been selected, continue. The result of this is that, if no checkbox is
            // selected, we send the message to all contacts in range.
            if (!contact.selectedCheckbox.isSelected() && countSelected > 0) continue;
            try {
                networkHandler.sendChatMessage(contact.userNick, messageToSend);
            } catch (AcquireTupleException | JMSException | IllegalAccessException e) {
                logMessage("Couldn't send message to " + contact + "!");
                e.printStackTrace();
            }
        }
    }

    @FXML
    void initialize() {
        clientData = ClientDataSingleton.getInstance();
        getNetworkHandlerOrExit();
        setUpLists();
        setUpErrors();
        updateContactList();
        setUpReadings();
    }

    private void getNetworkHandlerOrExit() {
        try {
            networkHandler = NetworkHandlerSingleton.getInstance();
        } catch (Exception e) {
            System.out.println("Could not get network handler!");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void setUpLists() {
        chatList = FXCollections.observableArrayList();
        chatListView.setItems(chatList);

        contactsList = FXCollections.observableArrayList();
        contactsListView.setItems(contactsList);
        contactsAdded = new HashSet<>();
    }

    private void setUpErrors() {
        onlineStatusErrorText.setOpacity(0);
        chatViewErrorText.setOpacity(0);
        contactListErrorText.setOpacity(0);
    }

    public static void logMessage(String message) {
        chatList.add(message);
    }

    public void updateContactList() {
        List<String> contactsToAdd = networkHandler.getNeighborhood();

        for (String userID : contactsToAdd) {
            addContactToListView(userID);
        }
    }

    private void setUpReadings() {
        setNewRadiusText(clientData.detectionRadius);
        setNewCoordinateText(latitudeText, "Latitude", clientData.initialLatitude);
        setNewCoordinateText(longitudeText, "Longitude", clientData.initialLongitude);
        setOnlineStatusText(clientData.initialOnlineStatus);
        isOnlineCheckBox.setSelected(clientData.initialOnlineStatus);
    }

    public static void addContactToListView(String contactNick) {
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
        if (contactsAdded.contains(contactNick) || contactNick.equals(clientData.userNick)) return;

        notifyContactInRange(contactNick);

        contactsAdded.add(contactNick);
        String contactName = NetworkHandlerSingleton.getInstance().getUserName(contactNick);
        ContactEntryHBox hBox = setUpHBox(contactNick, contactName);
        contactsList.add(hBox);
    }

    private static ContactEntryHBox setUpHBox(String contactNick, String contactName) {
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
        return hBox;
    }

    private static void notifyContactInRange(String contactNick) {
        try {
            System.out.println("Pinging user " + contactNick);
            NetworkHandlerSingleton.getInstance().pingUser(contactNick);
        } catch (AcquireTupleException e) {
            System.out.println("Couldn't ping user " + contactNick + "!");
            e.printStackTrace();
        }
    }

    @FXML
    void onRadiusAction() {
        int radius = -1;
        try {
            radius = getValueFromField(contactRadiusField);
        } catch (NumberFormatException e) {
            showMessageTemporarilyOnField(contactRadiusField, "Valor inválido!");
            return;
        }
        if (radius < 0) {
            showMessageTemporarilyOnField(contactRadiusField, "Valor inválido!");
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
        updatePositionFromField(latitudeField);
    }

    @FXML
    void onLongitudeFieldAction() {
        updatePositionFromField(longitudeField);
    }

    private void updatePositionFromField(TextField field) {
        Integer coordinate = getCoordinateOrNull(field);
        if (coordinate == null) return;
        try {
            updateMyUser(coordinate, null, null);
        } catch (WriteTupleException e) {
            showMessageTemporarilyOnField(field, "Falha na escrita!");
            e.printStackTrace();
            return;
        }
        updateContactList();
        setCoordinateReadingText(field, coordinate);
        field.clear();
    }

    private void setCoordinateReadingText(TextField field, Integer coordinate) {
        if (field == longitudeField) {
            setNewCoordinateText(longitudeText, "Longitude", coordinate);
        } else {
            setNewCoordinateText(latitudeText, "Latitude", coordinate);
        }
    }

    private Integer getCoordinateOrNull(TextField field) {
        int coordinate = -1;
        try {
            coordinate = getValueFromField(field);
        } catch (NumberFormatException e) {
            showMessageTemporarilyOnField(field, "Valor inválido!");
            return null;
        }
        return coordinate;
    }

    int getValueFromField(TextField field) {
        return Integer.parseInt(field.getText());
    }

    void showMessageTemporarilyOnField(TextField field, String message) {
        field.setText(message);
        showTemporaryError(field);
    }

    private void showTemporaryError(TextField field) {
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
        try {
            updateMyUser(null, null, isOnline);
        } catch (WriteTupleException e) {
            showErrorOnOnlineStatusText();
        }
        updateContactList();
        setOnlineStatusText(isOnline);
    }

    private void showErrorOnOnlineStatusText() {
        onlineStatusErrorText.setText("Falha na escrita!");
        onlineStatusErrorText.setOpacity(1);
        Timer refreshTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                onlineStatusErrorText.setOpacity(0);
            }
        });
        refreshTimer.setRepeats(false);
        refreshTimer.start();
    }

    void updateMyUser(Integer longitude, Integer latitude, Boolean isOnline) throws WriteTupleException {
        try {
            networkHandler.updateMyUser(latitude, longitude, isOnline);
            getQueuedMessagesIfOnline(isOnlineCheckBox.isSelected());
        } catch (Exception e) {
            throw new WriteTupleException();
        }
    }

    private void setNewCoordinateText(Text text, String coordinateName, int coordinate) {
        text.setText(coordinateName + " (atual: " + coordinate + ")");
    }

    private void setNewRadiusText(int newRadius) {
        radiusText.setText("Raio (atual: " + newRadius + ")");
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
                    String destination = messageParts[2];
                    if (!clientData.userNick.equals(destination)) continue;
                    String message = messageParts[3];
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
