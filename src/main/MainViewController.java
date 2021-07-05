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
import net.jini.core.transaction.TransactionException;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;
import utils.tuples.AuctionTrackerTuple;
import utils.tuples.UserTuple;

import java.rmi.RemoteException;

public class MainViewController {

    @FXML
    private ListView<HBox> auctionListView;
    private ObservableList<HBox> auctionList;

    @FXML
    private ListView<HBox> myAuctionsListView;
    private ObservableList<HBox> myAuctionsList;

    @FXML
    private TextField auctionIdField;

    @FXML
    private TextArea auctionDescriptionField;

    @FXML
    private Text mainListErrorText;

    @FXML
    private Text myListErrorText;

    @FXML
    private Text auctionIDErrorText;
    
    @FXML
    private Text auctionCreateErrorText;

    private NetworkHandlerSingleton networkHandler;
    private ClientDataSingleton clientData;

    @FXML
    void initialize() {
        clientData = ClientDataSingleton.getInstance();
        try {
            networkHandler = NetworkHandlerSingleton.getInstance();
            prepareSpaceForAuction();
        } catch (Exception e) {
            System.out.println("Could not prepare JavaSpace for auctions!");
            e.printStackTrace();
            System.exit(0);
        }
        setUpLists();
        updateLists();
        setUpErrors();
    }

    private void setUpErrors() {
        auctionIDErrorText.setOpacity(0);
        mainListErrorText.setOpacity(0);
        myListErrorText.setOpacity(0);
        auctionCreateErrorText.setOpacity(0);
    }

    private void setUpLists() {
        auctionList = FXCollections.observableArrayList();
        auctionListView.setItems(auctionList);

        myAuctionsList = FXCollections.observableArrayList();
        myAuctionsListView.setItems(myAuctionsList);
    }

    @FXML
    void createAuctionButtonClick() {
        if (!inputIsValid()) {
            auctionIDErrorText.setOpacity(1);
            return;
        }
        auctionIDErrorText.setOpacity(0);
        writeAuction();
        updateLists();
        auctionIdField.clear();
        auctionDescriptionField.clear();
    }

    private boolean inputIsValid() {
        return !auctionIdField.getText().trim().equals("");
    }

    private void writeAuction() {
        try {
            networkHandler.writeAuction(auctionIdField.getText(), auctionDescriptionField.getText(), clientData.userName);
        } catch (WriteTupleException e) {
            auctionCreateErrorText.setText("Não foi possível escrever os dados remotos!");
            auctionCreateErrorText.setOpacity(1);
            e.printStackTrace();
            return;
        } catch (AcquireTupleException e) {
            auctionCreateErrorText.setText("Não foi possível ler os dados remotos!");
            auctionCreateErrorText.setOpacity(1);
            e.printStackTrace();
            return;
        }
        auctionCreateErrorText.setOpacity(0);
    }
    
    @FXML
    void updateViewsButtonClick() {
        updateLists();
    }

    private void updateLists() {
        updateMyListUI();
        updateMainListUI();
    }

    private void updateMainListUI() {
        try {
            updateMainList();
            mainListErrorText.setOpacity(0);
        } catch (Exception e) {
            mainListErrorText.setOpacity(1);
            e.printStackTrace();
        }
    }

    private void updateMyListUI() {
        try {
            updateMyList();
            myListErrorText.setOpacity(0);
        } catch (Exception e) {
            myListErrorText.setOpacity(1);
            e.printStackTrace();
        }
    }

    private void updateMyList() throws AcquireTupleException {
        UserTuple myUser = networkHandler.getMyUserWithHandler(networkHandler);

        myAuctionsList.clear();
        for (String auction : myUser.madeAuctions) {
            addEntryToMyAuctionList(auction, "0");
        }
    }

    private void updateMainList() throws AcquireTupleException {
        AuctionTrackerTuple auctionTracker = networkHandler.readAuctionTracker(6000);
        if (auctionTracker == null) {
            System.out.println("Didn't get the auction tracker!");
            throw new AcquireTupleException();
        }
        auctionList.clear();
        for (String auction : auctionTracker.auctionList) {
            addEntryToMainAuctionList(auction);
        }
    }

    private void prepareSpaceForAuction() throws WriteTupleException, AcquireTupleException {
        if (!networkHandler.auctionTrackerExists()) {
            networkHandler.writeAuctionTracker();
        }
    }

    public void addEntryToMyAuctionList(String auctionID, String bidCount) {
        Text text = new Text();
        text.setText(auctionID + " (" + bidCount + " lances)");
        Region spacer = new Region();
        Button button = new Button();
        button.setText("Excluir");
        button.setOnMouseClicked(mouseEvent -> {
            try {
                NetworkHandlerSingleton.getInstance().deleteAuctionWithID(auctionID);
                updateLists();
            } catch (AcquireTupleException e) {
                System.out.println("Couldn't acquire tuple to be deleted!");
                e.printStackTrace();
            } catch (WriteTupleException e) {
                e.printStackTrace(); // TODO: Review this
            }
        });
        HBox box = new HBox();
        box.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(text, spacer, button);
        myAuctionsList.add(box);
        System.out.println("Added entry to my list");
    }

    private void addEntryToMainAuctionList(String auctionId) {
        Text text = new Text();
        text.setText(auctionId);
        Region spacer = new Region();
        Button button = new Button();
        button.setText("Visualizar/Adicionar lance");
        button.setOnMouseClicked(mouseEvent -> openDetailsForAuction(auctionId));
        HBox box = new HBox();
        box.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(text, spacer, button);
        auctionList.add(box);
    }

    private void openDetailsForAuction(String auctionID) {
        ClientDataSingleton.getInstance().setLastClickedID(auctionID);
        openDetailsWindow();
    }

    private void openDetailsWindow() {
        try {
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("auctionDetails.fxml"));
            primaryStage.setTitle("Detalhes do lote");
            primaryStage.setScene(new Scene(root, 800, 500));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
