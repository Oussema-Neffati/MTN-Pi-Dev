package tn.esprit.controllers;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import tn.esprit.models.Evenement;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.Evenementservice;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class interfaceevenement {

    private Evenementservice evenementService;

    @FXML
    private TextField searchField;
    @FXML
    private Label userNameLabel;

    @FXML
    private VBox eventVBox;

    private Evenement selectedEvenement;

    public interfaceevenement() {
        this.evenementService = new Evenementservice();
    }

    @FXML
    public void initialize() {
        refreshVBox();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue.trim();
            ObservableList<Evenement> searchResults = evenementService.searchEvents(query);
            updateVBox(searchResults);
        });

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


    private void refreshVBox() {
        ObservableList<Evenement> evenements = evenementService.readAll();
        updateVBox(evenements);
    }

    private void updateVBox(ObservableList<Evenement> evenements) {
        eventVBox.getChildren().clear();
        selectedEvenement = null;

        for (Evenement evenement : evenements) {
            HBox eventRow = new HBox(10);
            eventRow.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 10; -fx-border-color: #DDDDDD; -fx-border-width: 1;");

            Label nomLabel = new Label(evenement.getNom());
            nomLabel.setPrefWidth(115);
            Label lieuLabel = new Label(evenement.getLieu());
            lieuLabel.setPrefWidth(170);
            Label dateLabel = new Label(evenement.getDate());
            dateLabel.setPrefWidth(134);
            Label organisateurLabel = new Label(evenement.getOrganisateur());
            organisateurLabel.setPrefWidth(127);
            Label prixLabel = new Label(String.valueOf(evenement.getPrix()));
            prixLabel.setPrefWidth(134);


            eventRow.getChildren().addAll(nomLabel, lieuLabel, dateLabel, organisateurLabel,prixLabel);

            eventRow.setOnMouseClicked(event -> {
                for (HBox child : eventVBox.getChildren().filtered(node -> node instanceof HBox).toArray(HBox[]::new)) {
                    child.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 10; -fx-border-color: #DDDDDD; -fx-border-width: 1;");
                }
                eventRow.setStyle("-fx-background-color: #E0F7FA; -fx-padding: 10; -fx-border-color: #DDDDDD; -fx-border-width: 1;");
                selectedEvenement = evenement;
            });

            eventVBox.getChildren().add(eventRow);
        }
    }

    @FXML
    private void handleAjouterEvenement(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/inter_event_ajout.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.setTitle("Ajouter un Événement");

        stage.showAndWait();
        refreshVBox();
    }

    @FXML
    private void handleModifierEvenement(ActionEvent event) throws IOException, SQLException {
        if (selectedEvenement == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun événement sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un événement à modifier.");
            alert.showAndWait();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/inter_event_modif.fxml"));
        Parent root = loader.load();

        inter_event_modif controller = loader.getController();
        controller.setEvenementToModify(selectedEvenement);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.setTitle("Modifier un Événement");

        stage.showAndWait();
        refreshVBox();
    }

    @FXML
    private void handleSupprimerEvenement(ActionEvent event) throws SQLException {
        if (selectedEvenement == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun événement sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un événement à supprimer.");
            alert.showAndWait();
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer cet événement ?");
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            evenementService.delete(selectedEvenement.getId());
            refreshVBox();
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText(null);
            success.setContentText("Événement supprimé avec succès !");
            success.showAndWait();
        }
    }

    @FXML
    private void handleGestionEvenement(ActionEvent event) {
        refreshVBox();
    }
    @FXML
    private void handleVoirParticipation(ActionEvent event) throws IOException {
        if (selectedEvenement == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun événement sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un événement pour voir les participations.");
            alert.showAndWait();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/participation.fxml"));
        Parent root = loader.load();

        participation controller = loader.getController();
        controller.setEvent(selectedEvenement);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(true);
        stage.setScene(new Scene(root));
        stage.setTitle("Participations à l'événement: " + selectedEvenement.getNom());

        stage.showAndWait();
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