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
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class UserProfileController {
    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField roleField;

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
    private TextField departementField;

    @FXML
    private TextField dateEmbaucheField;

    @FXML
    private Button saveButton;

    @FXML
    private Button backButton;

    @FXML
    private Button changePasswordButton;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private Utilisateur currentUser;

    @FXML
    void initialize() {
        // Récupérer l'utilisateur connecté
        currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur connecté.");
            goBack(new ActionEvent());
            return;
        }

        // Remplir les champs communs
        nomField.setText(currentUser.getNom());
        prenomField.setText(currentUser.getPrenom());
        emailField.setText(currentUser.getEmail());
        roleField.setText(currentUser.getRole().name());

        // Afficher/masquer les champs spécifiques selon le rôle
        if (currentUser.getRole() == Role.CITOYEN) {
            citoyenFields.setVisible(true);
            employeFields.setVisible(false);

            // Récupérer les informations du citoyen
            Citoyen citoyen = serviceUtilisateur.getCitoyenById(currentUser.getId());
            if (citoyen != null) {
                cinField.setText(citoyen.getCin());
                adresseField.setText(citoyen.getAdresse());
                telephoneField.setText(citoyen.getTelephone());
            }
        } else if (currentUser.getRole() == Role.EMPLOYE) {
            citoyenFields.setVisible(false);
            employeFields.setVisible(true);

            // Récupérer les informations de l'employé
            Employe employe = serviceUtilisateur.getEmployeById(currentUser.getId());
            if (employe != null) {
                posteField.setText(employe.getPoste());
                departementField.setText(employe.getDepartement());
                dateEmbaucheField.setText(employe.getDateEmbauche().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        } else {
            // Cas de l'administrateur
            citoyenFields.setVisible(false);
            employeFields.setVisible(false);
        }
    }

    @FXML
    void saveProfile(ActionEvent event) {
        // Validation des champs
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Les champs Nom et Prénom sont obligatoires.");
            return;
        }

        // Mettre à jour les informations communes
        currentUser.setNom(nomField.getText());
        currentUser.setPrenom(prenomField.getText());

        if (currentUser.getRole() == Role.CITOYEN) {
            // Validation des champs spécifiques au citoyen
            if (cinField.getText().isEmpty() || adresseField.getText().isEmpty() ||
                    telephoneField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                        "Tous les champs spécifiques au citoyen sont obligatoires.");
                return;
            }

            // Mettre à jour Citoyen
            Citoyen citoyen = serviceUtilisateur.getCitoyenById(currentUser.getId());
            if (citoyen != null) {
                citoyen.setNom(nomField.getText());
                citoyen.setPrenom(prenomField.getText());
                citoyen.setCin(cinField.getText());
                citoyen.setAdresse(adresseField.getText());
                citoyen.setTelephone(telephoneField.getText());

                // Mettre à jour
                serviceUtilisateur.update(citoyen);

                // Mettre à jour les données spécifiques au citoyen
                String qry = "UPDATE `citoyen` SET `cin`=?, `adresse`=?, `telephone`=? WHERE `id_user`=?";
                try {
                    PreparedStatement pstm = serviceUtilisateur.getCnx().prepareStatement(qry);
                    pstm.setString(1, citoyen.getCin());
                    pstm.setString(2, citoyen.getAdresse());
                    pstm.setString(3, citoyen.getTelephone());
                    pstm.setInt(4, citoyen.getId());

                    pstm.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } else if (currentUser.getRole() == Role.EMPLOYE) {
            // Validation des champs spécifiques à l'employé
            if (posteField.getText().isEmpty() || departementField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                        "Les champs Poste et Département sont obligatoires.");
                return;
            }

            // Mettre à jour Employe
            Employe employe = serviceUtilisateur.getEmployeById(currentUser.getId());
            if (employe != null) {
                employe.setNom(nomField.getText());
                employe.setPrenom(prenomField.getText());
                employe.setPoste(posteField.getText());
                employe.setDepartement(departementField.getText());

                // Mettre à jour
                serviceUtilisateur.update(employe);

                // Mettre à jour les données spécifiques à l'employé
                String qry = "UPDATE `employe` SET `poste`=?, `departement`=? WHERE `id_user`=?";
                try {
                    PreparedStatement pstm = serviceUtilisateur.getCnx().prepareStatement(qry);
                    pstm.setString(1, employe.getPoste());
                    pstm.setString(2, employe.getDepartement());
                    pstm.setInt(3, employe.getId());

                    pstm.executeUpdate();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }
        } else {
            // Cas de l'administrateur
            serviceUtilisateur.update(currentUser);
        }

        // Mettre à jour l'utilisateur dans la session
        SessionManager.getInstance().setCurrentUser(currentUser);

        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "Profil mis à jour avec succès.");
    }

    @FXML
    void changePassword(ActionEvent event) {
        // Afficher une boîte de dialogue pour changer le mot de passe
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Changer le mot de passe");
        dialog.setHeaderText("Entrez votre nouveau mot de passe");

        // Créer les champs de la boîte de dialogue
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Ancien mot de passe");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le nouveau mot de passe");

        // Ajouter les champs à la boîte de dialogue
        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Ancien mot de passe"), oldPasswordField,
                new Label("Nouveau mot de passe"), newPasswordField,
                new Label("Confirmer le nouveau mot de passe"), confirmPasswordField));

        // Ajouter les boutons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Ajouter un gestionnaire d'événements pour le bouton OK
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                // Vérifier que l'ancien mot de passe est correct
                if (!oldPasswordField.getText().equals(currentUser.getMotDePasse())) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "L'ancien mot de passe est incorrect.");
                    return null;
                }

                // Vérifier que les deux nouveaux mots de passe correspondent
                if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Les nouveaux mots de passe ne correspondent pas.");
                    return null;
                }

                // Mettre à jour le mot de passe
                currentUser.setMotDePasse(newPasswordField.getText());
                serviceUtilisateur.update(currentUser);

                // Mettre à jour l'utilisateur dans la session
                SessionManager.getInstance().setCurrentUser(currentUser);

                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Mot de passe changé avec succès.");

                return newPasswordField.getText();
            }
            return null;
        });

        // Afficher la boîte de dialogue
        dialog.showAndWait();
    }

    @FXML
    void goBack(ActionEvent event) {
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
}