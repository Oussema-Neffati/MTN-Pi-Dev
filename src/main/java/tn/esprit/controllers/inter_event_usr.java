package tn.esprit.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.models.Evenement;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.Evenementservice;

import java.io.IOException;

import javafx.scene.control.ScrollPane;
import javafx.scene.web.WebView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.esprit.services.paiementStripe;
import tn.esprit.utils.SessionManager;

public class inter_event_usr {

    private Evenementservice evenementService;

    @FXML
    private TextField searchField;

    @FXML
    private ScrollPane eventScrollPane;

    @FXML
    private Label userNameLabel;

    public inter_event_usr() {
        this.evenementService = new Evenementservice();
    }

    @FXML
    public void initialize() {
        refreshScrollPane();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue.trim();
            ObservableList<Evenement> searchResults = evenementService.searchEvents(query);
            updateScrollPane(searchResults);
        });
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

    private void refreshScrollPane() {
        ObservableList<Evenement> evenements = evenementService.readAll();
        updateScrollPane(evenements);
    }

    private void updateScrollPane(ObservableList<Evenement> evenements) {
        FlowPane cardContainer = new FlowPane(20, 20); // 20px spacing between cards
        cardContainer.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20;");
        cardContainer.setPrefWrapLength(598); // Width to fit 2 cards per row

        for (Evenement evenement : evenements) {
            VBox card = new VBox(15);
            card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-border-radius: 15; -fx-background-radius: 15; -fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 3);");
            card.setPrefWidth(280); // Increased width for larger cards
            card.setPrefHeight(220); // Increased height for better design

            // Event details
            Label nomLabel = new Label(evenement.getNom());
            nomLabel.setStyle("-fx-text-fill: #DB4495; -fx-font-size: 18; -fx-font-weight: bold;");

            Label dateLabel = new Label("Date: " + evenement.getDate());
            dateLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14;");

            Label lieuLabel = new Label("Lieu: " + evenement.getLieu());
            lieuLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 14;");

            // Status indicator
            HBox statusBox = new HBox(10);
            Circle statusIndicator = new Circle(8);

            Label availabilityLabel = new Label();

            if (evenement.getNombreplace() > 0) {
                statusIndicator.setFill(Color.GREEN);
                availabilityLabel.setText("Disponible (" + evenement.getNombreplace() + " places)");
                availabilityLabel.setStyle("-fx-text-fill: #178A17; -fx-font-size: 14; -fx-font-weight: bold;");
            } else {
                statusIndicator.setFill(Color.RED);
                availabilityLabel.setText("Complet (0 place)");
                availabilityLabel.setStyle("-fx-text-fill: #D13030; -fx-font-size: 14; -fx-font-weight: bold;");
            }

            statusBox.getChildren().addAll(statusIndicator, availabilityLabel);
            HBox.setHgrow(availabilityLabel, Priority.ALWAYS);

            // Details button
            Button detailsBtn = new Button("Détails");
            detailsBtn.setStyle("-fx-background-color: #4169E1; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 8 15 8 15;");
            detailsBtn.setPrefWidth(100);

            detailsBtn.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/inter_event_details.fxml"));
                    Parent root = loader.load();

                    inter_event_details controller = loader.getController();
                    controller.setEvenement(evenement);

                    Stage stage = (Stage) detailsBtn.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Détails de l'événement");
                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible de charger la page de détails: " + ex.getMessage());
                    alert.showAndWait();
                }
            });

            card.getChildren().addAll(nomLabel, dateLabel, lieuLabel, statusBox, detailsBtn);
            cardContainer.getChildren().add(card);
        }

        eventScrollPane.setContent(cardContainer);
        eventScrollPane.setFitToWidth(true);
        eventScrollPane.setStyle("-fx-background-color: transparent;");
    }

    @FXML
    private void handlePayButton() {
        try {
            double totalAmount = 0;

            if (totalAmount <= 0) {
                showError("Erreur de montant", "Aucun abonnement non payé trouvé.", "Veuillez vérifier les abonnements.");
                return;
            }

            paiementStripe stripePaymentService = new paiementStripe();
            String paymentUrl = stripePaymentService.createCheckoutSession(totalAmount);

            WebView webView = new WebView();
            webView.getEngine().load(paymentUrl);

            VBox webViewContainer = new VBox(webView);
            Scene scene = new Scene(webViewContainer, 800, 600);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Paiement Stripe");

            webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.contains("success")) {
                    stage.close();
                } else if (newValue != null && newValue.contains("cancel")) {
                    stage.close();
                    showError("Annulation", "Le paiement a été annulé.", "Veuillez réessayer.");
                }
            });

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de paiement", "Une erreur s'est produite lors du paiement.", e.getMessage());
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de profil.");
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'écran de connexion.");
        }
    }
}