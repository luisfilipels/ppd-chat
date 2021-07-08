package utils;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

public class ContactEntryHBox extends HBox {

    public final String userNick;
    public final String userName;
    public final CheckBox selectedCheckbox;

    public ContactEntryHBox(String userNick, String userName, CheckBox selectedCheckbox) {
        super();
        this.userNick = userNick;
        this.userName = userName;
        this.selectedCheckbox = selectedCheckbox;
    }

}
