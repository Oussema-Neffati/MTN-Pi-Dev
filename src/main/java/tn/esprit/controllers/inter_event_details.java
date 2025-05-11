package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.esprit.models.Evenement;
import tn.esprit.models.Participation;
import tn.esprit.services.ParticipationService;
import tn.esprit.services.paiementStripe;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.util.Optional;

public class inter_event_details {

    @FXML
    private Label nomLabel;

    @FXML
    private Label lieuLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label organisateurLabel;

    @FXML
    private Label prixLabel1;

    private Evenement evenement;
    private ParticipationService participationService;
    private paiementStripe stripePaymentService;

    // L'ID du citoyen connecté (à remplacer par la logique d'authentification réelle)
    private int currentUserId = 1; // Valeur temporaire pour démonstration

    @FXML
    public void initialize() {
        participationService = new ParticipationService();
        stripePaymentService = new paiementStripe();
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;
        displayEvenementDetails();
    }

    private void displayEvenementDetails() {
        if (evenement != null) {
            nomLabel.setText("Nom d'événement: " + evenement.getNom());
            lieuLabel.setText("Lieu: " + evenement.getLieu());
            dateLabel.setText("Date: " + evenement.getDate());
            organisateurLabel.setText("Organisateur: " + evenement.getOrganisateur());
            prixLabel1.setText("Prix: " + evenement.getPrix());
        }
    }

    @FXML
    private void retourusr(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/inter_event_usr.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) nomLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Événements");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", null, "Impossible de retourner à la liste des événements: " + e.getMessage());
        }
    }

    @FXML
    private void handlePayerMaintenant(ActionEvent event) {
        // Vérifier si l'utilisateur participe déjà à cet événement
        if (participationService.participationExists(currentUserId, evenement.getId())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Déjà inscrit");
            alert.setHeaderText(null);
            alert.setContentText("Vous êtes déjà inscrit à cet événement.");
            alert.showAndWait();
            return;
        }

        // Confirmer le paiement
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de paiement");
        confirmAlert.setHeaderText("Voulez-vous participer à cet événement?");
        confirmAlert.setContentText("Prix: " + evenement.getPrix() + " DT");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processPayment();
        }
    }

    private void processPayment() {
        try {
            // Créer une session de paiement Stripe
            double amount = evenement.getPrix();
            String paymentUrl = stripePaymentService.createCheckoutSession(amount);

            // Ouvrir l'URL de paiement dans un WebView
            WebView webView = new WebView();
            webView.getEngine().load(paymentUrl);

            // Afficher le WebView dans une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setScene(new Scene(webView, 800, 600));
            stage.setTitle("Paiement Stripe");

            // Écouter les changements d'URL pour détecter le succès ou l'annulation
            webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.contains("success")) {
                    stage.close(); // Fermer la fenêtre WebView

                    // Créer une nouvelle participation avec le statut "Payé"
                    Participation participation = new Participation(evenement.getId(), currentUserId, "Payé");

                    if (participationService.add(participation)) {
                        showSuccess("Paiement réussi", "Votre participation a été enregistrée avec succès!",
                                "Vous êtes maintenant inscrit à l'événement : " + evenement.getNom());
                    } else {
                        showError("Erreur d'enregistrement", "Le paiement a réussi mais nous n'avons pas pu enregistrer votre participation.",
                                "Veuillez contacter le support.");
                    }
                } else if (newValue.contains("cancel")) {
                    stage.close(); // Fermer la fenêtre WebView
                    showInfo("Paiement annulé", null, "Le paiement a été annulé.");
                }
            });

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de paiement", "Une erreur s'est produite lors du paiement.", e.getMessage());
        }
    }

    @FXML
    private void handleEvenements(ActionEvent event) {
        // Navigation vers la vue générale des événements
        retourusr(event);
    }

    // Méthodes utilitaires pour les alertes
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
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
    }@FXML
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
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger la page de profil.");
        }
    }


}