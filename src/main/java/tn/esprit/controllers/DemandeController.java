package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import tn.esprit.models.Demande;
import tn.esprit.Service.ServiceDemande;

import java.sql.SQLException;

public class DemandeController {

    @FXML
    private TextField idUserField;
    @FXML
    private TextField nomField;
    @FXML
    private TextField typeDocumentField;
    @FXML
    private TextField adresseField;
    @FXML
    private TextField prixField;

    private final ServiceDemande serviceDemande = new ServiceDemande();

    @FXML
    private void handleSubmit(ActionEvent event) {
        String idUserText = idUserField.getText();
        String nom = nomField.getText();
        String type = typeDocumentField.getText();
        String adresse = adresseField.getText();
        String prixText = prixField.getText();

        if (!validateInputs(idUserText, nom, type, adresse, prixText)) {
            return;
        }

        try {
            int idUser = Integer.parseInt(idUserText);
            float prix = Float.parseFloat(prixText);

            Demande demande = new Demande();
            demande.setId_user(idUser);
            demande.setNom(nom);
            demande.setType(type);
            demande.setAdresse(adresse);
            demande.setPrice(prix);

            serviceDemande.ajouter(demande);

            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Demande ajoutée avec succès ! ID: " + demande.getId_demande());

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Format numérique invalide !");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void handleFetchUserInfo(ActionEvent event) {
        String idUserText = idUserField.getText();

        if (idUserText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champ manquant", "Veuillez saisir l'ID utilisateur !");
            return;
        }

        try {
            int idUser = Integer.parseInt(idUserText);
            Demande tempDemande = new Demande();
            tempDemande.setId_user(idUser);

            serviceDemande.setUserNameInDemande(tempDemande);

            if (tempDemande.getNom() != null && !tempDemande.getNom().isEmpty()) {
                nomField.setText(tempDemande.getNom());
            } else {
                showAlert(Alert.AlertType.WARNING, "Utilisateur introuvable", "Aucun utilisateur trouvé avec cet ID !");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Format d'ID utilisateur invalide !");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de la récupération : " + e.getMessage());
        }
    }

    private boolean validateInputs(String idUser, String nom, String type, String adresse, String prix) {
        if (idUser.isEmpty() || nom.isEmpty() || type.isEmpty() || adresse.isEmpty() || prix.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Champs manquants", "Tous les champs sont obligatoires !");
            return false;
        }

        try {
            Integer.parseInt(idUser);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format invalide", "L'ID utilisateur doit être un nombre !");
            return false;
        }

        try {
            Float.parseFloat(prix);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format invalide", "Le prix doit être un nombre !");
            return false;
        }

        return true;
    }

    private void clearFields() {
        idUserField.clear();
        nomField.clear();
        typeDocumentField.clear();
        adresseField.clear();
        prixField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}