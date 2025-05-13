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

public class inter_event_ajout {

    @FXML
    private TextField nomEvenementField;
    @FXML
    private TextField lieuEvenementField;
    @FXML
    private DatePicker dateEvenementPicker;
    @FXML
    private TextField organisateurField;
    @FXML
    private Label userNameLabel;
    @FXML
    private TextField prixField1;
    @FXML
    private TextField nombreplaceField11;

    private Button mapButton;

    private Evenementservice evenementService;
    private map mapController;

    public inter_event_ajout() {
        evenementService = new Evenementservice();
    }

    @FXML
    public void initialize() {
        mapController = new map();
        mapController.setLinkedAddressField(lieuEvenementField);

        if (mapButton != null) {
            mapButton.setOnAction(event -> handleOpenMap());
        }
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        } else {
            userNameLabel.setText("Non connecté");
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
    private void onRetourButtonClick(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/FXML/interfaceevenement.fxml"));
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Municipalité Tunisienne Électronique");
        stage.show();
        ((Stage) ((javafx.scene.control.Button) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void onAjouterButtonClick() {
        String nomEvenement = nomEvenementField.getText();
        String lieuEvenement = lieuEvenementField.getText();
        LocalDate dateEvenement = dateEvenementPicker.getValue();
        String organisateur = organisateurField.getText();
        float prix = Float.parseFloat(prixField1.getText().trim());
        int totalPlaces = Integer.parseInt(nombreplaceField11.getText().trim());

        if (nomEvenement.isEmpty() || lieuEvenement.isEmpty() || dateEvenement == null || organisateur.isEmpty() || prix <= 0 || totalPlaces <= 10) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs !");
            alert.showAndWait();
            return;
        }

        String dateFormatted = dateEvenement.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Evenement evenement = new Evenement();
        evenement.setNom(nomEvenement);
        evenement.setLieu(lieuEvenement);
        evenement.setDate(dateFormatted);
        evenement.setOrganisateur(organisateur);
        evenement.setPrix(prix);
        evenement.setNombreplace(totalPlaces); // Initial available places
        evenement.setTotalPlaces(totalPlaces); // Set total places

        evenementService.create(evenement);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Événement ajouté avec succès :\n" +
                "Nom : " + nomEvenement + "\n" +
                "Lieu : " + lieuEvenement + "\n" +
                "Date : " + dateEvenement + "\n" +
                "Organisateur : " + organisateur + "\n" +
                "Prix : " + prix +
                "Nombre total des places : " + totalPlaces);
        alert.showAndWait();

        nomEvenementField.clear();
        lieuEvenementField.clear();
        dateEvenementPicker.setValue(null);
        organisateurField.clear();
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
            showAlert1(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de profil.");
        }
    }

    @FXML
    void logout(ActionEvent event) {
        SessionManager.getInstance().clearSession();
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
            showAlert1(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'écran de connexion.");
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