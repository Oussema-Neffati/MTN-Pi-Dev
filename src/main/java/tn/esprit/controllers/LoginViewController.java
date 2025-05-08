package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.NavigationUtils;
import tn.esprit.utils.SessionManager;
import tn.esprit.utils.logUtils;

import java.io.IOException;

public class LoginViewController {

    @FXML
    private TextField EmailTF;

    @FXML
    private Button loginB;

    @FXML
    private TextField passwordTF;

    @FXML
    private Hyperlink signup;

    @FXML
    void initialize() {
        // Ajouter un gestionnaire d'événements au bouton de connexion
        loginB.setOnAction(this::login);

        // Ajouter un gestionnaire d'événements au lien d'inscription
        signup.setOnAction(this::signup);
    }

    @FXML
    void signup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/SignupView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage;
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger l'écran d'inscription.");
        }
    }

    private void loadAdminDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AdminDashboard.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Administration - Gestion des utilisateurs");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger le dashboard administrateur.");
        }
    }

    private void loadMainInterface(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Interface1.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Municipalité Tunisienne Electronique");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger l'interface principale.");
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
    void login(ActionEvent event) {
        String email = EmailTF.getText();
        String password = passwordTF.getText();

        // Validation des champs...

        // Cas spécial pour l'administrateur
        if (email.equals("admin@mairie.tn") && password.equals("admin123")) {
            // Créer et stocker l'utilisateur admin...

            // Journaliser la connexion réussie
            logUtils.logAuthEvent(email, true, "Connexion administrateur");

            // Naviguer vers le dashboard admin
            NavigationUtils.loadView(event, "/FXML/AdminDashboard.fxml", "Administration - Gestion des utilisateurs");
            return;
        }

        // Pour les autres utilisateurs
        ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
        Utilisateur user = serviceUtilisateur.authenticate(email, password);

        if (user != null) {
            // Stocker l'utilisateur et naviguer...

            // Journaliser la connexion réussie
            logUtils.logAuthEvent(email, true, "Connexion " + user.getRole().name());

        } else {
            // Essayer de déterminer la cause de l'échec
            Utilisateur inactiveUser = serviceUtilisateur.findByEmail(email);

            if (inactiveUser != null && !inactiveUser.isActif()) {
                logUtils.logAuthEvent(email, false, "Compte inactif");
                showAlert(Alert.AlertType.WARNING, "Compte inactif",
                        "Votre compte est en attente d'activation par un administrateur.");
            } else {
                logUtils.logAuthEvent(email, false, "Identifiants incorrects");
                showAlert(Alert.AlertType.ERROR, "Erreur d'authentification",
                        "Email ou mot de passe incorrect.");
            }
        }
    }
}