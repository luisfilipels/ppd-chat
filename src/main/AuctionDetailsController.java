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

    private void showConnectionError(String s) {
        connectionIssueErrorText.setText(s);
        connectionIssueErrorText.setOpacity(1);
    }


}
