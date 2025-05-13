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
import tn.esprit.models.Citoyen;
import tn.esprit.models.Utilisateur;
import tn.esprit.models.Evenement;
import tn.esprit.models.Participation;
import tn.esprit.services.ParticipationService;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class participation {

    @FXML
    private TextField searchField;

    @FXML
    private VBox participationVBox;

    @FXML
    private Label eventNameLabel;

    @FXML
    private Label userNameLabel;

    @FXML
    private Button closeButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button logoutButton;

    private ParticipationService participationService;
    private ServiceUtilisateur serviceUtilisateur;
    private Evenement selectedEvent;
    private ObservableList<Participation> participations;

    public participation() {
        this.participationService = new ParticipationService();
        this.serviceUtilisateur = new ServiceUtilisateur();
    }

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterParticipations(newValue);
        });
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
        } else {
            userNameLabel.setText("Non connecté");
        }
    }

    public void setEvent(Evenement event) {
        this.selectedEvent = event;
        eventNameLabel.setText("Participations à l'événement: " + event.getNom());
        System.out.println("Event ID: " + event.getId() + ", Event Name: " + event.getNom());
        loadParticipations();
    }

    private void loadParticipations() {
        participations = participationService.getApprovedParticipationsByEvent(selectedEvent.getId());
        System.out.println("Loaded " + participations.size() + " approved participations for event ID: " + selectedEvent.getId());
        displayParticipations(participations);
    }

    private void filterParticipations(String searchText) {
        FilteredList<Participation> filteredData = new FilteredList<>(participations, p -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }

            String lowerCaseFilter = searchText.toLowerCase();
            try {
                Citoyen user = (Citoyen) serviceUtilisateur.getCitoyenById(p.getId_user());
                if (user != null) {
                    return user.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                            user.getNom().toLowerCase().contains(lowerCaseFilter) ||
                            (user.getCin() != null && user.getCin().toLowerCase().contains(lowerCaseFilter)) ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerCaseFilter));
                } else {
                    System.err.println("User not found for ID: " + p.getId_user());
                }
            } catch (Exception e) {
                System.err.println("Error during user search for ID " + p.getId_user() + ": " + e.getMessage());
            }
            return false;
        });

        displayParticipations(filteredData);
    }

    private void displayParticipations(ObservableList<Participation> participationsToDisplay) {
        participationVBox.getChildren().clear();

        // Header row
        HBox header = new HBox(10);
        header.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 5;");
        Label prenomHeader = new Label("Prénom");
        prenomHeader.setPrefWidth(92);
        prenomHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: #db4495; -fx-font-weight: bold;");

        Label nomHeader = new Label("Nom");
        nomHeader.setPrefWidth(88);
        nomHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: #db4495; -fx-font-weight: bold;");

        Label cinHeader = new Label("CIN");
        cinHeader.setPrefWidth(97);
        cinHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: #db4495; -fx-font-weight: bold;");

        Label emailHeader = new Label("Email");
        emailHeader.setPrefWidth(159);
        emailHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: #db4495; -fx-font-weight: bold;");

        Label statusHeader = new Label("Statut");
        statusHeader.setPrefWidth(80);
        statusHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: #db4495; -fx-font-weight: bold;");

        Label datePayHeader = new Label("Date de paiement");
        datePayHeader.setPrefWidth(120);
        datePayHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: #db4495; -fx-font-weight: bold;");

        header.getChildren().addAll(prenomHeader, nomHeader, cinHeader, emailHeader, statusHeader, datePayHeader);
        participationVBox.getChildren().add(header);

        // Check if there are participations
        if (participationsToDisplay.isEmpty()) {
            HBox noDataRow = new HBox(10);
            noDataRow.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 5;");
            Label noDataLabel = new Label("Aucun participant payé trouvé pour cet événement.");
            noDataLabel.setPrefWidth(636); // Adjusted for new column (92 + 88 + 97 + 159 + 80 + 120 = 636)
            noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
            noDataRow.getChildren().add(noDataLabel);
            participationVBox.getChildren().add(noDataRow);
            System.out.println("No approved participations found for display.");
            return;
        }

        // Data rows
        for (Participation participation : participationsToDisplay) {
            HBox row = new HBox(10);
            row.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 5;");

            try {
                System.out.println("Fetching user for participation ID: " + participation.getIdParticipation() + ", User ID: " + participation.getId_user());
                Citoyen user = (Citoyen) serviceUtilisateur.getCitoyenById(participation.getId_user());
                if (user != null) {
                    System.out.println("User found: " + user.getPrenom() + " " + user.getNom());
                    Label prenomLabel = new Label(user.getPrenom() != null ? user.getPrenom() : "Non spécifié");
                    prenomLabel.setPrefWidth(92);
                    prenomLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

                    Label nomLabel = new Label(user.getNom() != null ? user.getNom() : "Non spécifié");
                    nomLabel.setPrefWidth(88);
                    nomLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

                    Label cinLabel = new Label(user.getCin() != null ? user.getCin() : "Non spécifié");
                    cinLabel.setPrefWidth(97);
                    cinLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

                    Label emailLabel = new Label(user.getEmail() != null ? user.getEmail() : "Non spécifié");
                    emailLabel.setPrefWidth(159);
                    emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

                    Label statusLabel = new Label("Payé");
                    statusLabel.setPrefWidth(80);
                    statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4CAF50;");

                    // Add Date de paiement
                    Label datePayLabel = new Label(participation.getDatepay() != null ?
                            participation.getDatepay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "Non spécifiée");
                    datePayLabel.setPrefWidth(120);
                    datePayLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

                    row.getChildren().addAll(prenomLabel, nomLabel, cinLabel, emailLabel, statusLabel, datePayLabel);
                } else {
                    System.err.println("User not found for ID: " + participation.getId_user());
                    Label errorLabel = new Label("Utilisateur introuvable (ID: " + participation.getId_user() + ")");
                    errorLabel.setPrefWidth(636); // Adjusted for new column
                    errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF5555;");
                    row.getChildren().add(errorLabel);
                }
            } catch (Exception e) {
                System.err.println("Error loading user for participation ID " + participation.getIdParticipation() + ": " + e.getMessage());
                Label errorLabel = new Label("Erreur de chargement (ID: " + participation.getId_user() + ")");
                errorLabel.setPrefWidth(636); // Adjusted for new column
                errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF5555;");
                row.getChildren().add(errorLabel);
            }

            participationVBox.getChildren().add(row);
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page de profil.");
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'écran de connexion.");
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