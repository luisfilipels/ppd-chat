package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;
import utils.tuples.BatchTuple;

import java.util.Map;

public class AuctionDetailsController {

    @FXML
    private Text auctionIdText;

    @FXML
    private Text auctionDescriptionText;

    @FXML
    private ListView<Text> bidListView;
    private ObservableList<Text> bidList;

    @FXML
    private TextField bidValueField;

    @FXML
    private CheckBox publicBidCheckBox;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Text auctionCreatorText;

    @FXML
    private Text invalidBidErrorText;

    @FXML
    private Text connectionIssueErrorText;

    private String auctionID;
    private NetworkHandlerSingleton networkHandler;
    private ClientDataSingleton clientData;

    @FXML
    void cancelButtonClick(ActionEvent event) {
        closeSelfWindow(event);
    }

    @FXML
    void confirmButtonClick(ActionEvent event) {
        try {
            if (!createAuction()) return;
            closeSelfWindow(event);
        } catch (AcquireTupleException e) {
            showConnectionError("Problema ao receber dados!");
            e.printStackTrace();
        } catch (WriteTupleException e) {
            showConnectionError("Problema ao enviar dados!");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            invalidBidErrorText.setOpacity(1);
        }
    }

    private void showConnectionError(String s) {
        connectionIssueErrorText.setText(s);
        connectionIssueErrorText.setOpacity(1);
    }

    private boolean createAuction() throws AcquireTupleException, WriteTupleException {
        int value = Integer.parseInt(bidValueField.getText());
        if (value <= 0) throw new NumberFormatException();
        invalidBidErrorText.setOpacity(0);
        boolean isPublic = publicBidCheckBox.isSelected();
        BatchTuple thisBatch = networkHandler.takeAuctionTuple(auctionID, 5000);
        if (thisBatch == null) {
            System.out.println("Couldn't get batch information!");
            return false;
        }
        thisBatch.addBid(ClientDataSingleton.getInstance().userName, value, isPublic);
        networkHandler.writeAuction(thisBatch);
        return true;
    }

    private void closeSelfWindow(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void initialize() {
        acquireHandlers();
        setUpLists();
        setUpAuctionID();
        setUpAuctionCreatorName();
        setUpErrorTexts();
        try {
            setAuctionDescriptionText();
        } catch (Exception e) {
            auctionDescriptionText.setText("Não foi possível carregar a descrição!");
            e.printStackTrace();
        }
        updateBidsList();
    }

    private void setUpErrorTexts() {
        invalidBidErrorText.setOpacity(0);
        connectionIssueErrorText.setOpacity(0);
    }

    private void acquireHandlers() {
        networkHandler = NetworkHandlerSingleton.getInstance();
        clientData = ClientDataSingleton.getInstance();
    }

    private void setUpAuctionID() {
        auctionID = clientData.getLastClickedID();
        auctionIdText.setText(auctionID);
    }

    private void setUpAuctionCreatorName() {
        try {
            BatchTuple thisBatch = networkHandler.readAuction(clientData.getLastClickedID());
            if (thisBatch.sellerId.equals(clientData.userName)) {
                auctionCreatorText.setText(thisBatch.sellerId + " (Você)");
            } else {
                auctionCreatorText.setText(thisBatch.sellerId);
            }
        } catch (AcquireTupleException e) {
            e.printStackTrace();
        }
    }

    private void setUpLists() {
        bidList = FXCollections.observableArrayList();
        bidListView.setItems(bidList);
    }

    private void setAuctionDescriptionText() throws Exception{
        BatchTuple thisBatch = networkHandler.readAuction(auctionID);
        if (thisBatch == null) throw new AcquireTupleException();
        auctionDescriptionText.wrappingWidthProperty().bind(scrollPane.widthProperty());
        auctionDescriptionText.setText(thisBatch.description);
    }

    private void updateBidsList() {
        try {
            BatchTuple thisBatch = networkHandler.readAuction(auctionID);
            if (thisBatch == null) throw new AcquireTupleException();
            if (thisBatch.bids == null) throw new NullPointerException();
            for (Map.Entry<String, String> entry : thisBatch.bids.entrySet()) {
                addEntryToList(thisBatch, entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addEntryToList(BatchTuple thisBatch, Map.Entry<String, String> entry) {
        String[] bidData = entry.getValue().split("\\|");

        int value = Integer.parseInt(bidData[0]);
        boolean isPublic = bidData[1].equals("true");

        String bidCreator = entry.getKey();
        if (bidCreator.equals(clientData.userName)) {
            // I created the bid, so I should absolutely see the bid
            bidList.add(new Text("Criador: Você" + getPrivateText(isPublic) + " valor: " + value));
        } else {
            // I didn't create the bid, so I should only see the bid if it is public,
            // or if I created the auction
            if (isPublic || thisBatch.sellerId.equals(clientData.userName)) {
                bidList.add(new Text("Criador: " + bidCreator + getPrivateText(isPublic) + " valor: " + value));
            }
        }
    }

    private String getPrivateText(boolean isPublic) {
        return isPublic ? "," : " (privado),";
    }


}
