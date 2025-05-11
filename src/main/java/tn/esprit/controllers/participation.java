package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Evenement;
import tn.esprit.models.Participation;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ParticipationService;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class participation {

    @FXML
    private TextField searchField;

    @FXML
    private VBox participationVBox;

    @FXML
    private Label eventNameLabel;

    @FXML
    private Button closeButton;

    private ParticipationService participationService;
    private ServiceUtilisateur ServiceUtilisateur;
    private Evenement selectedEvent;
    private ObservableList<Participation> participations;

    public participation() {
        this.participationService = new ParticipationService();
        this.ServiceUtilisateur = new ServiceUtilisateur();
    }

    @FXML
    public void initialize() {
        // Le champ de recherche sera configuré une fois que l'événement est défini
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterParticipations(newValue);
        });
    }

    public void setEvent(Evenement event) {
        this.selectedEvent = event;
        eventNameLabel.setText("Participations à l'événement: " + event.getNom());
        loadParticipations();
    }

    private void loadParticipations() {
        // Charger toutes les participations pour cet événement
        participations = participationService.getParticipationsByEvent(selectedEvent.getId());
        displayParticipations(participations);
    }

    private void filterParticipations(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            displayParticipations(participations);
            return;
        }

        FilteredList<Participation> filteredData = new FilteredList<>(participations, p -> {
            // Si le filtre est vide, afficher tout
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = searchText.toLowerCase();

            // Essayer de trouver le nom d'utilisateur correspondant
            try {
                Utilisateur user = ServiceUtilisateur.getCitoyenById(p.getId_user());
                if (user != null && user.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la recherche d'utilisateur: " + e.getMessage());
            }

            // Vérifier si le statut correspond
            return p.getStatut().toLowerCase().contains(lowerCaseFilter);
        });

        displayParticipations(filteredData);
    }

    private void displayParticipations(ObservableList<Participation> participationsToDisplay) {
        participationVBox.getChildren().clear();

        for (Participation participation : participationsToDisplay) {
            HBox row = new HBox(10);
            row.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 10; -fx-border-color: #DDDDDD; -fx-border-width: 1;");

            // ID de participation
            Label idLabel = new Label(String.valueOf(participation.getIdParticipation()));
            idLabel.setPrefWidth(60);

            // Nom de l'utilisateur (on essaie de le récupérer depuis le service)
            String userName = "Utilisateur " + participation.getId_user();
            try {
                Utilisateur user = ServiceUtilisateur.getCitoyenById(participation.getId_user());
                if (user != null) {
                    userName = user.getNom();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
            }
            Label userLabel = new Label(userName);
            userLabel.setPrefWidth(180);

            // Statut de la participation
            Label statutLabel = new Label(participation.getStatut());
            statutLabel.setPrefWidth(220);

            // Boutons d'action
            HBox actionsBox = new HBox(5);
            Button approveBtn = new Button("Approuver");
            approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

            Button rejectBtn = new Button("Refuser");
            rejectBtn.setStyle("-fx-background-color: #F44336; -fx-text-fill: white;");

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white;");

            actionsBox.getChildren().addAll(approveBtn, rejectBtn, deleteBtn);

            // Configuration des actions des boutons
            approveBtn.setOnAction(event -> {
                updateParticipationStatus(participation, "Approuvé");
            });

            rejectBtn.setOnAction(event -> {
                updateParticipationStatus(participation, "Refusé");
            });

            deleteBtn.setOnAction(event -> {
                deleteParticipation(participation);
            });

            // Ajouter tous les éléments à la ligne
            row.getChildren().addAll(idLabel, userLabel, statutLabel, actionsBox);
            participationVBox.getChildren().add(row);
        }
    }

    private void updateParticipationStatus(Participation participation, String newStatus) {
        try {
            participationService.updateStatut(participation.getId_user(), participation.getIdEvenement(), newStatus);

            // Rafraîchir l'affichage
            loadParticipations();

            // Afficher un message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Statut mis à jour");
            alert.setHeaderText(null);
            alert.setContentText("Le statut de la participation a été mis à jour avec succès!");
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de la mise à jour du statut: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void deleteParticipation(Participation participation) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer cette participation?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                participationService.delete(participation.getIdParticipation());

                // Rafraîchir l'affichage
                loadParticipations();

                // Afficher un message de confirmation
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Participation supprimée");
                alert.setHeaderText(null);
                alert.setContentText("La participation a été supprimée avec succès!");
                alert.showAndWait();

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de la suppression de la participation: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    public void handleCloseButton(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
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
            showAlert(Alert.AlertType.ERROR, "Erreur",
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
}