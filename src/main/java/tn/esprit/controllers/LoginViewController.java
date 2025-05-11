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
import tn.esprit.utils.PreferencesUtils;
import tn.esprit.utils.SessionManager;
import tn.esprit.utils.ValidationUtils;
import tn.esprit.utils.logUtils;

import tn.esprit.services.GoogleSignInService;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.Optional;

public class LoginViewController {

    @FXML
    private TextField EmailTF;

    @FXML
    private Button loginB;

    @FXML
    private PasswordField passwordTF;

    @FXML
    private Hyperlink signup;

    @FXML
    private CheckBox rememberMeCheckBox;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    void initialize() {
        // Ajouter un gestionnaire d'événements au bouton de connexion
        loginB.setOnAction(this::login);

        // Ajouter un gestionnaire d'événements au lien d'inscription
        signup.setOnAction(this::signup);

        // Charger les identifiants sauvegardés si "Se souvenir de moi" était activé
        loadSavedCredentials();
    }

    private void loadSavedCredentials() {
        if (PreferencesUtils.isRememberMeChecked()) {
            EmailTF.setText(PreferencesUtils.getSavedEmail());
            passwordTF.setText(PreferencesUtils.getSavedPassword());
            rememberMeCheckBox.setSelected(true);
        }
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

    @FXML
    void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ForgotPasswordView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Mot de passe oublié");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger l'écran de récupération de mot de passe.");
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

        // Validation des champs
        if (ValidationUtils.isNullOrEmpty(email) || ValidationUtils.isNullOrEmpty(password)) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Veuillez remplir tous les champs.");
            return;
        }

        // Sauvegarder ou supprimer les identifiants selon la case à cocher
        if (rememberMeCheckBox.isSelected()) {
            PreferencesUtils.saveCredentials(email, password, true);
        } else {
            PreferencesUtils.clearSavedCredentials();
        }

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
                // Naviguer vers le nouveau dashboard avec cards
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/AdminDashboardCards.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Administration - Gestion des utilisateurs");
                stage.show();
            } catch (IOException e) {
                System.err.println("Erreur de chargement: " + e.getMessage());
                e.printStackTrace();
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

    @FXML
    private Button googleSignInButton;


    @FXML
    void googleSignIn(ActionEvent event) {
        try {
            String[] userInfo = GoogleSignInService.getUserInfo();
            if (userInfo != null && userInfo.length >= 2) {
                String email = userInfo[0];
                String name = userInfo[1];

                // Vérifier si l'utilisateur existe dans la base de données
                ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
                Utilisateur user = serviceUtilisateur.findByEmail(email);

                if (user != null && user.isActif()) {
                    // L'utilisateur existe, on le connecte
                    SessionManager.getInstance().setCurrentUser(user);

                    // Journaliser la connexion
                    logUtils.logAuthEvent(email, true, "Connexion Google " + user.getRole().name());

                    NavigationUtils.loadView(event, "/FXML/Interface1.fxml", "Municipalité Tunisienne Electronique");
                } else {
                    // L'utilisateur n'existe pas, on affiche une option pour créer un compte
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Compte non trouvé");
                    alert.setHeaderText("Aucun compte associé à cet email Google");
                    alert.setContentText("Voulez-vous créer un nouveau compte avec cet email ?");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        // Rediriger vers le formulaire d'inscription avec l'email pré-rempli
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/SignupView.fxml"));
                        Parent root = loader.load();

                        SignupViewController controller = loader.getController();
                        // Préremplir l'email
                        controller.setEmailFromGoogle(email);

                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Inscription");
                        stage.show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de se connecter avec Google: " + e.getMessage());
        }
    }
}