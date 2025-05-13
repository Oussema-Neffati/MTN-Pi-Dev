package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Ressource;
import tn.esprit.services.ServiceRessource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

public class RessourceController {

    @FXML
    private TextField nomField;

    @FXML
    private ComboBox<String> categorieField;

    @FXML
    private TextField capaciteField;

    @FXML
    private TextField tarifHoraireField;

    @FXML
    private TextField horaireOuvertureField;

    @FXML
    private TextField horaireFermetureField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private CheckBox disponibleCheckBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button backButton;

    private ServiceRessource serviceRessource = new ServiceRessource();
    private Ressource currentRessource;

    @FXML
    void initialize() {
        // Initialize with a new resource or load an existing one
        currentRessource = new Ressource();

        // Populate the ComboBox with categories (example categories, adjust as needed)
        categorieField.getItems().addAll("Salle de réunion", "Véhicule", "Équipement", "Espace public");
        categorieField.setPromptText("Sélectionner une catégorie");

        // Clear fields
        nomField.setText("");
        capaciteField.setText("");
        tarifHoraireField.setText("");
        horaireOuvertureField.setText("");
        horaireFermetureField.setText("");
        descriptionField.setText("");
        disponibleCheckBox.setSelected(true);
    }

    @FXML
    void saveRessource(ActionEvent event) {
        // Validation des champs
        if (nomField.getText().isEmpty() || categorieField.getValue() == null ||
                capaciteField.getText().isEmpty() || tarifHoraireField.getText().isEmpty() ||
                horaireOuvertureField.getText().isEmpty() || horaireFermetureField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Tous les champs obligatoires doivent être remplis.");
            return;
        }

        // Validate numeric fields
        int capacite;
        double tarifHoraire;
        try {
            capacite = Integer.parseInt(capaciteField.getText());
            if (capacite < 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                        "La capacité doit être un nombre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "La capacité doit être un nombre valide.");
            return;
        }

        try {
            tarifHoraire = Double.parseDouble(tarifHoraireField.getText());
            if (tarifHoraire < 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                        "Le tarif horaire doit être un nombre positif.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                    "Le tarif horaire doit être un nombre valide.");
            return;
        }

        // Mettre à jour les informations de la ressource
        currentRessource.setNom(nomField.getText());
        currentRessource.setCategorie(categorieField.getValue());
        currentRessource.setCapacite(capacite);
        currentRessource.setTarifHoraire(tarifHoraire);
        currentRessource.setHoraireOuverture(horaireOuvertureField.getText());
        currentRessource.setHoraireFermeture(horaireFermetureField.getText());
        currentRessource.setDescription(descriptionField.getText());
        currentRessource.setDisponible(disponibleCheckBox.isSelected());

        try {
            if (currentRessource.getId() == 0) {
                // Nouvelle ressource
                serviceRessource.insert(currentRessource);
            } else {
                // Mise à jour de la ressource existante
                serviceRessource.update(currentRessource);
            }
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Ressource enregistrée avec succès.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de l'enregistrement de la ressource: " + e.getMessage());
        }
    }

    @FXML
    void goBack(ActionEvent event) {
        navigateTo(event, "/FXML/Interface1.fxml", "Municipalité Tunisienne Electronique");
    }

    @FXML
    void navigateToDemande(ActionEvent event) {
        navigateTo(event, "/FXML/Demande.fxml", "Gestion des Demandes");
    }

    @FXML
    void navigateToReservations(ActionEvent event) {
        navigateTo(event, "/FXML/Reservations.fxml", "Gestion des Réservations");
    }

    @FXML
    void navigateToEvenements(ActionEvent event) {
        navigateTo(event, "/FXML/Evenements.fxml", "Gestion des Évènements");
    }

    @FXML
    void navigateToProjets(ActionEvent event) {
        navigateTo(event, "/FXML/Projets.fxml", "Gestion des Projets");
    }

    @FXML
    void navigateToParametres(ActionEvent event) {
        navigateTo(event, "/FXML/Parametres.fxml", "Paramètres");
    }

    @FXML
    void navigateToProfil(ActionEvent event) {
        navigateTo(event, "/FXML/Profil.fxml", "Mon Profil");
    }

    @FXML
    void navigateToLogout(ActionEvent event) {
        navigateTo(event, "/FXML/Login.fxml", "Connexion");
    }

    private void navigateTo(ActionEvent event, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger l'interface: " + fxmlPath);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void loadRessource(int ressourceId) {
        try {
            currentRessource = serviceRessource.readById(ressourceId);
            if (currentRessource != null) {
                nomField.setText(currentRessource.getNom());
                categorieField.setValue(currentRessource.getCategorie());
                capaciteField.setText(String.valueOf(currentRessource.getCapacite()));
                tarifHoraireField.setText(String.valueOf(currentRessource.getTarifHoraire()));
                horaireOuvertureField.setText(currentRessource.getHoraireOuverture());
                horaireFermetureField.setText(currentRessource.getHoraireFermeture());
                descriptionField.setText(currentRessource.getDescription());
                disponibleCheckBox.setSelected(currentRessource.isDisponible());
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Ressource non trouvée.");
                goBack(new ActionEvent());
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors du chargement de la ressource: " + e.getMessage());
        }
    }
}