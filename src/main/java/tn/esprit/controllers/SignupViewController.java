package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Citoyen;
import tn.esprit.models.Employe;
import tn.esprit.models.Role;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.ValidationUtils;

import java.io.IOException;
import java.time.LocalDate;

public class SignupViewController {
    @FXML
    private Button CreerCompte;

    @FXML
    private TextField EmailTFs;

    @FXML
    private TextField NomTFs;

    @FXML
    private TextField PrenomTFs;

    @FXML
    private ComboBox<Role> RoleCB;

    @FXML
    private Hyperlink gologinpage;

    @FXML
    private TextField passwordTFs;

    @FXML
    private VBox citoyenFields;

    @FXML
    private VBox employeFields;

    @FXML
    private TextField cinField;

    @FXML
    private TextField adresseField;

    @FXML
    private TextField telephoneField;

    @FXML
    private TextField posteField;

    @FXML
    private DatePicker dateEmbaucheField;

    @FXML
    private TextField departementField;

    @FXML
    void initialize() {
        // Exclure ADMIN des options du ComboBox
        RoleCB.getItems().addAll(Role.CITOYEN, Role.EMPLOYE);

        // Par défaut, sélectionner CITOYEN
        RoleCB.setValue(Role.CITOYEN);

        // Initialiser la visibilité des champs spécifiques
        updateFieldsVisibility(Role.CITOYEN);

        // Écouter les changements de rôle
        RoleCB.setOnAction(event -> {
            updateFieldsVisibility(RoleCB.getValue());
        });
    }

    private void updateFieldsVisibility(Role role) {
        if (role == Role.CITOYEN) {
            citoyenFields.setVisible(true);
            employeFields.setVisible(false);
        } else if (role == Role.EMPLOYE) {
            citoyenFields.setVisible(false);
            employeFields.setVisible(true);
        }
    }

    @FXML
    void gologinpage(ActionEvent event) {
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
    void CreerCompte(ActionEvent event) {
        // Valider les champs communs
        if (ValidationUtils.isNullOrEmpty(NomTFs.getText()) ||
                ValidationUtils.isNullOrEmpty(PrenomTFs.getText()) ||
                ValidationUtils.isNullOrEmpty(EmailTFs.getText()) ||
                ValidationUtils.isNullOrEmpty(passwordTFs.getText()) ||
                RoleCB.getValue() == null) {

            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Valider l'email
        if (!ValidationUtils.isValidEmail(EmailTFs.getText())) {
            showAlert(Alert.AlertType.ERROR, "Email invalide",
                    "Veuillez entrer une adresse email valide.");
            return;
        }

        // Valider le mot de passe
        if (!ValidationUtils.isValidPassword(passwordTFs.getText())) {
            showAlert(Alert.AlertType.ERROR, "Mot de passe invalide",
                    "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre.");
            return;
        }

        // Vérifier si l'email existe déjà
        ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
        if (serviceUtilisateur.findByEmail(EmailTFs.getText()) != null) {
            showAlert(Alert.AlertType.ERROR, "Email déjà utilisé",
                    "Cette adresse email est déjà associée à un compte.");
            return;
        }

        // Créer l'utilisateur en fonction du rôle
        Role selectedRole = RoleCB.getValue();

        if (selectedRole == Role.CITOYEN) {
            // Valider les champs spécifiques au citoyen
            if (ValidationUtils.isNullOrEmpty(cinField.getText()) ||
                    ValidationUtils.isNullOrEmpty(adresseField.getText()) ||
                    ValidationUtils.isNullOrEmpty(telephoneField.getText())) {

                showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                        "Veuillez remplir tous les champs spécifiques au citoyen.");
                return;
            }

            // Valider le CIN
            if (!ValidationUtils.isValidCIN(cinField.getText())) {
                showAlert(Alert.AlertType.ERROR, "CIN invalide",
                        "Le CIN doit contenir 8 chiffres.");
                return;
            }

            // Valider le téléphone
            if (!ValidationUtils.isValidPhone(telephoneField.getText())) {
                showAlert(Alert.AlertType.ERROR, "Téléphone invalide",
                        "Le numéro de téléphone doit contenir 8 chiffres et commencer par 2, 4, 5 ou 9.");
                return;
            }

            // Créer et ajouter le citoyen
            Citoyen citoyen = new Citoyen();
            citoyen.setNom(NomTFs.getText());
            citoyen.setPrenom(PrenomTFs.getText());
            citoyen.setEmail(EmailTFs.getText());
            citoyen.setMotDePasse(passwordTFs.getText());
            citoyen.setCin(cinField.getText());
            citoyen.setAdresse(adresseField.getText());
            citoyen.setTelephone(telephoneField.getText());
            citoyen.setActif(true); // Les citoyens sont activés par défaut

            serviceUtilisateur.addCitoyen(citoyen);

            showAlert(Alert.AlertType.INFORMATION, "Inscription réussie",
                    "Votre compte citoyen a été créé avec succès. Vous pouvez maintenant vous connecter.");

        } else if (selectedRole == Role.EMPLOYE) {
            // Suite du code pour les employés...
            // (similaire à ce qui a été présenté précédemment)
        }

        // Retourner à la page de connexion
        gologinpage(event);
    }
}