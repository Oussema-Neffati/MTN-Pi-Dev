package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.utils.SessionManager;

import java.io.IOException;

public class Interface1Controller {
    @FXML
    private Label userNameLabel;

    @FXML
    private Button profileButton;

    @FXML
    private Button logoutButton;

    @FXML
    void initialize() {
        // Vérifier que l'utilisateur est connecté
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        } else {
            userNameLabel.setText("Non connecté");
            // Rediriger vers la page de connexion si aucun utilisateur n'est connecté
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root);
                Stage stage = (Stage) userNameLabel.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Connexion");
                stage.show();
            } catch (IOException e) {
                System.err.println("Erreur de chargement: " + e.getMessage());
            }
        }
    }

    @FXML
    void showProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/UserProfile.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Mon Profil");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la page de profil.");
        }
    }

    @FXML
    void logout(ActionEvent event) {
        // Déconnecter l'utilisateur
        SessionManager.getInstance().clearSession();

        // Rediriger vers la page de connexion
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger l'écran de connexion.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleEvenements(ActionEvent event) {
        try {
            Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
            String fxmlPath;

            // Debugging: Vérifier l'utilisateur et son rôle
            if (currentUser == null) {
                System.err.println("Erreur: Aucun utilisateur connecté.");
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Aucun utilisateur connecté. Veuillez vous reconnecter.");
                return;
            }

            String userRole = String.valueOf(currentUser.getRole());
            System.out.println("Rôle de l'utilisateur: " + userRole); // Log pour débogage

            // Vérifier si l'utilisateur est un employeur (insensible à la casse)
            if (userRole != null && userRole.equalsIgnoreCase("Employe")) {
                fxmlPath = "/FXML/interfaceevenement.fxml"; // FXML pour les événements employeur
                System.out.println("Chargement de la page employeur: " + fxmlPath);
            } else {
                fxmlPath = "/FXML/inter_event_usr.fxml"; // FXML pour les événements utilisateur
                System.out.println("Chargement de la page utilisateur: " + fxmlPath);
            }

            // Charger la page correspondante
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Événements - Municipalité Tunisienne Électronique");
            stage.show();

            // Fermer la fenêtre actuelle
            ((Stage) ((Button) event.getSource()).getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la page des événements : " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la page des événements.");
        }
    }
}