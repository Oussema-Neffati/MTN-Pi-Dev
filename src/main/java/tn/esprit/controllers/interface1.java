package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class interface1 {

    @FXML
    private void handleEvenements(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/login.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Municipalité Tunisienne Électronique");
            stage.show();
            ((Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement de inter_event_usr.fxml : " + e.getMessage());
        }
    }
}