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
        // Récupère les valeurs des champs
        String nomEvenement = nomEvenementField.getText();
        String lieuEvenement = lieuEvenementField.getText();
        LocalDate dateEvenement = dateEvenementPicker.getValue();
        String organisateur = organisateurField.getText();
        float prix=Float.parseFloat(prixField1.getText());
        int nbrEvenement=Integer.parseInt(nombreplaceField11.getText());

        // Vérifie si tous les champs sont remplis
        if (nomEvenement.isEmpty() || lieuEvenement.isEmpty() || dateEvenement == null || organisateur.isEmpty() || prix <= 0 || nbrEvenement <= 10)
        {
            // Affiche une alerte si un champ est vide
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs !");
            alert.showAndWait();
            return;
        }

        // Formate la date pour correspondre au format attendu par la base de données
        String dateFormatted = dateEvenement.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Crée un objet Evenement
        Evenement evenement = new Evenement();
        evenement.setNom(nomEvenement);
        evenement.setLieu(lieuEvenement);
        evenement.setDate(dateFormatted);
        evenement.setOrganisateur(organisateur);
        evenement.setPrix(prix);
        evenement.setNombreplace(nbrEvenement);

        // Ajoute l'événement à la base de données
        evenementService.create(evenement);

        // Affiche un message de confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Événement ajouté avec succès :\n" +
                "Nom : " + nomEvenement + "\n" +
                "Lieu : " + lieuEvenement + "\n" +
                "Date : " + dateEvenement + "\n" +
                "Organisateur : " + organisateur + "\n" +
                "Prix : " + prix +
                "nombre des places" + nbrEvenement);
        alert.showAndWait();

        // Réinitialise les champs après l'ajout
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