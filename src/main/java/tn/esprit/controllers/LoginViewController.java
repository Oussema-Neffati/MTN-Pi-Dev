package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Role;
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
            // Créer et stocker l'utilisateur admin
            Utilisateur admin = new Utilisateur();
            admin.setId(1);
            admin.setNom("Admin");
            admin.setPrenom("Admin");
            admin.setEmail("admin@mairie.tn");
            admin.setMotDePasse("admin123");
            admin.setRole(Role.ADMIN);
            admin.setActif(true);

            // Stocker dans la session
            SessionManager.getInstance().setCurrentUser(admin);

            // Journaliser la connexion réussie
            logUtils.logAuthEvent(email, true, "Connexion administrateur");

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
                e.printStackTrace(); // Ajouter cette ligne pour voir l'erreur complète
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de charger le dashboard administrateur.");
            }
            return;
        }

        // Pour les autres utilisateurs
        ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
        Utilisateur user = serviceUtilisateur.authenticate(email, password);

        if (user != null) {
            // Stocker l'utilisateur dans la session
            SessionManager.getInstance().setCurrentUser(user);

            // Journaliser la connexion réussie
            logUtils.logAuthEvent(email, true, "Connexion " + user.getRole().name());

            // Naviguer vers l'interface principale
            NavigationUtils.loadView(event, "/FXML/Interface1.fxml", "Municipalité Tunisienne Electronique");
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