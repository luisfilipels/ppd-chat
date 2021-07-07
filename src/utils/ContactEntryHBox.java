package utils;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

public class ContactEntryHBox extends HBox {

    public final String userID;
    public final CheckBox selectedCheckbox;

    public ContactEntryHBox(String userID, CheckBox selectedCheckbox) {
        super();
        this.userID = userID;
        this.selectedCheckbox = selectedCheckbox;
    }

}
