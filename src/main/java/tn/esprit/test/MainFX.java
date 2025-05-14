package tn.esprit.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import static javafx.application.Application.launch;

public class MainFX extends Application {
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainFX.class.getResource("/FXML/Reservation.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            
            // Set minimum window size
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            
            // Set initial window size
            stage.setWidth(1200);
            stage.setHeight(800);
            
            stage.setScene(scene);
            stage.setTitle("Resource Management");
            stage.show();
            
            // Center the window on screen
            stage.centerOnScreen();
        } catch (IOException e) {
            System.out.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}


