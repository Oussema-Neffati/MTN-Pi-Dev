package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Evenement;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.Evenementservice;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class inter_event_modif {

    @FXML
    private TextField nomEvenementField;
    @FXML
    private TextField lieuEvenementField;
    @FXML
    private DatePicker dateEvenementPicker;
    @FXML
    private TextField organisateurField;
    @FXML
    private TextField prixField1;
    @FXML
    private Button modifierButton;
    @FXML
    private Button retourButton;
    @FXML
    private Button mapButton;
    @FXML
    private Label userNameLabel;

    private Evenement evenementToModify;
    private Evenementservice evenementService;
    private map mapController;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public inter_event_modif() {
        this.evenementService = new Evenementservice();
    }

    @FXML
    public void initialize() {
        // Initialiser le contrôleur de carte
        mapController = new map();
        mapController.setLinkedAddressField(lieuEvenementField);

        // Configurer l'événement du bouton de carte
        if (mapButton != null) {
            mapButton.setOnAction(event -> handleOpenMap());
        }
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

    public void setEvenementToModify(Evenement evenement) {
        this.evenementToModify = evenement;
        populateFields();
    }

    private void populateFields() {
        if (evenementToModify != null) {
            nomEvenementField.setText(evenementToModify.getNom());
            lieuEvenementField.setText(evenementToModify.getLieu());
            organisateurField.setText(evenementToModify.getOrganisateur());
            prixField1.setText(String.valueOf(evenementToModify.getPrix()));

            try {
                LocalDate date = LocalDate.parse(evenementToModify.getDate(), dateFormatter);
                dateEvenementPicker.setValue(date);
            } catch (DateTimeParseException e) {
                System.out.println("Error parsing date: " + e.getMessage());
                dateEvenementPicker.setValue(null);
            }
        }
    }

    @FXML
    private void onModifierButtonClick(ActionEvent event) {
        if (evenementToModify == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun événement sélectionné", "Veuillez sélectionner un événement à modifier.");
            return;
        }

        // Validate input
        String nom = nomEvenementField.getText().trim();
        String lieu = lieuEvenementField.getText().trim();
        String organisateur = organisateurField.getText().trim();
        float prix = Float.parseFloat(prixField1.getText().trim());
        LocalDate date = dateEvenementPicker.getValue();

        if (nom.isEmpty() || lieu.isEmpty() || organisateur.isEmpty() || date == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", null, "Veuillez remplir tous les champs.");
            return;
        }

        // Update the event
        evenementToModify.setNom(nom);
        evenementToModify.setLieu(lieu);
        evenementToModify.setOrganisateur(organisateur);
        evenementToModify.setDate(date.format(dateFormatter));
        evenementToModify.setPrix(prix);

        try {
            evenementService.update(evenementToModify);
            showAlert(Alert.AlertType.INFORMATION, "Succès", null, "Événement modifié avec succès !");
            closeWindow();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification", e.getMessage());
        }
    }

    @FXML
    private void onRetourButtonClick(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/FXML/interfaceevenement.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Municipalité Tunisienne Électronique");
        stage.show();
        ((Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void handleOpenMap() {
        try {
            if (lieuEvenementField == null) {
                System.err.println("lieuEvenementField is null");
                showAlert(Alert.AlertType.ERROR, "Erreur", null, "Champ de lieu non initialisé");
                return;
            }

            if (mapController == null) {
                mapController = new map();
                mapController.setLinkedAddressField(lieuEvenementField);
            }

            mapController.openMap();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", null, "Problème lors de l'ouverture de la carte: " + e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) retourButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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
            showAlert1(Alert.AlertType.ERROR, "Erreur",
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
            showAlert1(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger l'écran de connexion.");
        }
    }

    private void showAlert1(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}