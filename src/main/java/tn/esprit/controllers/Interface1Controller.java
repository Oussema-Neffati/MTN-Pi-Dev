package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class Interface1Controller {
    @FXML
    private Label userNameLabel;

    @FXML
    private Button profileButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label monthYearLabel;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Label todayEventsLabel;

    private LocalDate currentDate;

    @FXML
    void initialize() {
        // Vérifier que l'utilisateur est connecté
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

        // Initialiser le calendrier
        currentDate = LocalDate.now();
        updateCalendar();
        updateTodayEvents();
    }

    @FXML
    void previousMonth(ActionEvent event) {
        currentDate = currentDate.minusMonths(1);
        updateCalendar();
    }

    @FXML
    void nextMonth(ActionEvent event) {
        currentDate = currentDate.plusMonths(1);
        updateCalendar();
    }

    private void updateCalendar() {
        // Mettre à jour l'en-tête du mois et année
        String monthYear = currentDate.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + currentDate.getYear();
        monthYearLabel.setText(monthYear.substring(0, 1).toUpperCase() + monthYear.substring(1));

        // Effacer la grille actuelle
        calendarGrid.getChildren().clear();

        // Ajouter les jours de la semaine
        String[] daysOfWeek = {"LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(daysOfWeek[i]);
            dayLabel.setStyle("-fx-text-fill: #4169E1; -fx-font-weight: bold; -fx-font-size: 12px; -fx-alignment: center;");
            dayLabel.setPrefSize(35, 25);
            calendarGrid.add(dayLabel, i, 0);
        }

        // Obtenir le premier jour du mois
        LocalDate firstOfMonth = currentDate.withDayOfMonth(1);
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() - 1; // 0 = lundi

        // Obtenir le nombre de jours dans le mois
        int daysInMonth = currentDate.lengthOfMonth();

        // Ajouter les jours du mois
        LocalDate today = LocalDate.now();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = firstOfMonth.plusDays(day - 1);
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.setPrefSize(35, 35);
            dayLabel.setStyle("-fx-text-alignment: center; -fx-alignment: center;");

            // Mettre en évidence le jour actuel
            if (date.equals(today)) {
                dayLabel.setStyle("-fx-background-color: #4169E1; -fx-text-fill: white; " +
                        "-fx-background-radius: 5; -fx-alignment: center; -fx-font-weight: bold;");
            } else if (date.equals(currentDate)) {
                dayLabel.setStyle("-fx-border-color: #4169E1; -fx-border-width: 1; " +
                        "-fx-border-radius: 5; -fx-alignment: center;");
            }

            // Ajouter un style hover
            dayLabel.setOnMouseEntered(e -> {
                if (!date.equals(today)) {
                    dayLabel.setStyle("-fx-background-color: #f0f8ff; -fx-text-fill: #4169E1; " +
                            "-fx-background-radius: 5; -fx-alignment: center;");
                }
            });

            dayLabel.setOnMouseExited(e -> {
                if (!date.equals(today)) {
                    dayLabel.setStyle("-fx-text-alignment: center; -fx-alignment: center;");
                }
            });

            // Ajouter une action au clic
            LocalDate finalDate = date;
            dayLabel.setOnMouseClicked(e -> {
                currentDate = finalDate;
                updateCalendar();
                updateTodayEvents();
            });

            int row = (startDayOfWeek + day - 1) / 7 + 1;
            int col = (startDayOfWeek + day - 1) % 7;
            calendarGrid.add(dayLabel, col, row);
        }
    }

    private void updateTodayEvents() {
        // Simuler des événements pour la date sélectionnée
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH);
        String dateStr = currentDate.format(formatter);
        dateStr = dateStr.substring(0, 1).toUpperCase() + dateStr.substring(1);

        StringBuilder events = new StringBuilder();
        events.append("Événements pour ").append(dateStr).append(":\n\n");

        // Exemple d'événements (à remplacer par des données réelles)
        if (currentDate.equals(LocalDate.now())) {
            events.append("• 09:00 - Réunion d'équipe\n");
            events.append("• 14:30 - Rendez-vous mairie\n");
            events.append("• 16:00 - Appel important\n");
        } else if (currentDate.isAfter(LocalDate.now())) {
            events.append("• Aucun événement prévu\n");
        } else {
            events.append("• Journée passée\n");
        }

        todayEventsLabel.setText(events.toString());
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

    // Méthodes pour gérer les autres boutons de l'interface principale
    // À implémenter selon vos besoins
}