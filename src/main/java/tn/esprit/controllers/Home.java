package tn.esprit.controllers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Home extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Activer les propriétés de débogage pour WebView
            System.setProperty("javafx.verbose", "true");
            System.setProperty("prism.verbose", "true");
            System.setProperty("javafx.debug", "true");

            // Paramètres pour éviter certains problèmes avec JavaFX WebView
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
            System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");

            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Demande.fxml"));
            if (loader.getLocation() == null) {
                throw new IllegalStateException("FXML file not found at /Views/Demande.fxml");
            }
            Parent root = loader.load();
            primaryStage.setTitle("Application de Demande");
            primaryStage.setScene(new Scene(root));
            primaryStage.setMinWidth(700);
            primaryStage.setMinHeight(500);
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}