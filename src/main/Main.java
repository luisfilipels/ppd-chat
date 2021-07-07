package main;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Stage popup = new Stage();
        popup.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("popupName.fxml")
        );
        popup.initStyle(StageStyle.UTILITY);
        Parent root2 = loader.load();
        popup.setTitle("Leitura de dados");
        popup.setResizable(false);
        popup.setScene(new Scene(root2, 400, 400));
        popup.show();
        popup.toFront();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
