package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import static javafx.application.Application.launch;


public class MainFX extends Application {
    public void start(Stage stage) {

        FXMLLoader fxmlLoader = new FXMLLoader(MainFX.class.getResource("/FXML/LoginView.fxml"));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load());

        stage.setScene(scene);
        stage.setTitle("Login");
        stage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String[] args) {launch();}

}


