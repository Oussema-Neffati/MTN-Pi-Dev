package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class ReservationController {

    // Form components
    @FXML
    private DatePicker dateReservationPicker;
    @FXML
    private ComboBox<String> heureDebutComboBox;
    @FXML
    private ComboBox<String> heureFinComboBox;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TextField nombreParticipantsField;
    @FXML
    private TextField motifField;
    @FXML
    private TextField cinField;

    // Card container
    @FXML
    private FlowPane reservationCardsContainer;

    // Data list
    private ObservableList<Reservation> reservationList;

    @FXML
    public void initialize() {
        // Initialize status ComboBox
        statusComboBox.setItems(FXCollections.observableArrayList("Confirmée", "En attente", "Annulée"));

        // Initialize time ComboBoxes with hourly slots (00:00 to 23:00)
        ObservableList<String> timeSlots = FXCollections.observableArrayList(
                IntStream.range(0, 24)
                        .mapToObj(hour -> String.format("%02d:00", hour))
                        .collect(Collectors.toList())
        );
        heureDebutComboBox.setItems(timeSlots);
        heureFinComboBox.setItems(timeSlots);

        // Initialize reservation list
        reservationList = FXCollections.observableArrayList();
        
        // Add listeners for time validation
        heureDebutComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateTimeSelection());
        heureFinComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateTimeSelection());
        dateReservationPicker.valueProperty().addListener((obs, oldVal, newVal) -> validateTimeSelection());
    }

    private void validateTimeSelection() {
        if (dateReservationPicker.getValue() != null && 
            heureDebutComboBox.getValue() != null && 
            heureFinComboBox.getValue() != null) {
            
            LocalDate selectedDate = dateReservationPicker.getValue();
            LocalTime startTime = LocalTime.parse(heureDebutComboBox.getValue() + ":00");
            LocalTime endTime = LocalTime.parse(heureFinComboBox.getValue() + ":00");

            // Check if end time is after start time
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "L'heure de fin doit être après l'heure de début");
                heureFinComboBox.setValue(null);
                return;
            }

            // Check for time conflicts with existing reservations
            boolean hasConflict = reservationList.stream()
                .filter(r -> r.getDateReservation().equals(selectedDate))
                .anyMatch(r -> {
                    LocalTime existingStart = r.getHeureDebut();
                    LocalTime existingEnd = r.getHeureFin();
                    return !(startTime.isAfter(existingEnd) || endTime.isBefore(existingStart));
                });

            if (hasConflict) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Il existe déjà une réservation pour cette période");
                heureDebutComboBox.setValue(null);
                heureFinComboBox.setValue(null);
            }
        }
    }

    @FXML
    private void saveReservation() {
        try {
            Reservation reservation = createReservationFromForm();
            reservationList.add(reservation);
            createReservationCard(reservation);
            resetForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation enregistrée avec succès");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    private void createReservationCard(Reservation reservation) {
        VBox card = new VBox(10);
        card.getStyleClass().add("reservation-card");
        
        // Title (Date)
        Label dateLabel = new Label(reservation.getDateReservation().toString());
        dateLabel.getStyleClass().add("title");
        
        // Status badge
        Label statusLabel = new Label(reservation.getStatus());
        statusLabel.getStyleClass().addAll("status", getStatusStyleClass(reservation.getStatus()));
        
        // Details
        VBox details = new VBox(5);
        details.getStyleClass().add("details");
        details.getChildren().addAll(
            new Label("Horaire: " + reservation.getHeureDebut() + " - " + reservation.getHeureFin()),
            new Label("Participants: " + reservation.getNombreParticipants()),
            new Label("CIN: " + reservation.getCin()),
            new Label("Motif: " + reservation.getMotif())
        );
        
        // Buttons
        HBox buttons = new HBox(10);
        buttons.getStyleClass().add("card-buttons");
        buttons.setAlignment(Pos.CENTER);
        
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(e -> populateForm(reservation));
        
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> {
            reservationList.remove(reservation);
            reservationCardsContainer.getChildren().remove(card);
        });
        
        buttons.getChildren().addAll(editButton, deleteButton);
        
        // Add all elements to card
        card.getChildren().addAll(dateLabel, statusLabel, details, buttons);
        
        // Add hover effect
        setupHoverEffect(card);
        
        // Add card to container
        reservationCardsContainer.getChildren().add(card);
    }

    private void setupHoverEffect(Node node) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), node);
        scaleIn.setToX(1.02);
        scaleIn.setToY(1.02);
        
        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), node);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);
        
        node.setOnMouseEntered(e -> scaleIn.playFromStart());
        node.setOnMouseExited(e -> scaleOut.playFromStart());
    }

    private String getStatusStyleClass(String status) {
        switch (status) {
            case "Confirmée": return "status-confirmed";
            case "En attente": return "status-pending";
            case "Annulée": return "status-cancelled";
            default: return "";
        }
    }

    @FXML
    private void updateReservation() {
        VBox selectedReservation = reservationCardsContainer.getChildren().stream()
            .map(node -> (VBox) node)
            .filter(vbox -> vbox.getChildren().get(0) instanceof Label)
            .map(vbox -> (Label) vbox.getChildren().get(0))
            .filter(label -> label.getText().equals(dateReservationPicker.getValue().toString()))
            .findFirst()
            .map(label -> reservationCardsContainer.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .filter(node -> ((VBox) node).getChildren().get(0) instanceof Label)
                .filter(node -> ((Label) ((VBox) node).getChildren().get(0)).getText().equals(label.getText()))
                .findFirst()
                .map(node -> (VBox) node)
                .orElse(null)
            )
            .orElse(null);

        if (selectedReservation != null) {
            try {
                Reservation updatedReservation = createReservationFromForm();
                updatedReservation.setId(reservationList.indexOf(reservationCardsContainer.getChildren().indexOf(selectedReservation)));
                reservationList.set(reservationList.indexOf(reservationCardsContainer.getChildren().indexOf(selectedReservation)), updatedReservation);
                resetForm();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation modifiée avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez sélectionner une réservation à modifier");
        }
    }

    @FXML
    private void deleteReservation() {
        VBox selectedReservation = reservationCardsContainer.getChildren().stream()
            .map(node -> (VBox) node)
            .filter(vbox -> vbox.getChildren().get(0) instanceof Label)
            .map(vbox -> (Label) vbox.getChildren().get(0))
            .filter(label -> label.getText().equals(dateReservationPicker.getValue().toString()))
            .findFirst()
            .map(label -> reservationCardsContainer.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .filter(node -> ((VBox) node).getChildren().get(0) instanceof Label)
                .filter(node -> ((Label) ((VBox) node).getChildren().get(0)).getText().equals(label.getText()))
                .findFirst()
                .map(node -> (VBox) node)
                .orElse(null)
            )
            .orElse(null);

        if (selectedReservation != null) {
            reservationList.remove(reservationCardsContainer.getChildren().indexOf(selectedReservation));
            resetForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation supprimée avec succès");
        } else {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez sélectionner une réservation à supprimer");
        }
    }

    // Helper method to create reservation from form
    private Reservation createReservationFromForm() {
        LocalDate date = dateReservationPicker.getValue();
        if (date == null) throw new IllegalArgumentException("La date est requise");

        String heureDebutStr = heureDebutComboBox.getValue();
        if (heureDebutStr == null) throw new IllegalArgumentException("L'heure de début est requise");
        LocalTime heureDebut = LocalTime.parse(heureDebutStr + ":00");

        String heureFinStr = heureFinComboBox.getValue();
        if (heureFinStr == null) throw new IllegalArgumentException("L'heure de fin est requise");
        LocalTime heureFin = LocalTime.parse(heureFinStr + ":00");

        String status = statusComboBox.getValue();
        if (status == null) throw new IllegalArgumentException("Le statut est requis");

        String nombreParticipantsStr = nombreParticipantsField.getText();
        if (nombreParticipantsStr.isEmpty()) throw new IllegalArgumentException("Le nombre de participants est requis");
        int nombreParticipants = Integer.parseInt(nombreParticipantsStr);

        String motif = motifField.getText();
        if (motif.isEmpty()) throw new IllegalArgumentException("Le motif est requis");

        String cin = cinField.getText();
        if (cin.isEmpty()) throw new IllegalArgumentException("Le CIN est requis");

        return new Reservation(
                reservationList.size() + 1,
                date,
                heureDebut,
                heureFin,
                status,
                nombreParticipants,
                motif,
                cin
        );
    }

    // Helper method to populate form with selected reservation
    private void populateForm(Reservation reservation) {
        dateReservationPicker.setValue(reservation.getDateReservation());
        // Format the time properly for the ComboBox values
        String heureDebutStr = String.format("%02d:00", reservation.getHeureDebut().getHour());
        String heureFinStr = String.format("%02d:00", reservation.getHeureFin().getHour());

        heureDebutComboBox.setValue(heureDebutStr);
        heureFinComboBox.setValue(heureFinStr);
        statusComboBox.setValue(reservation.getStatus());
        nombreParticipantsField.setText(String.valueOf(reservation.getNombreParticipants()));
        motifField.setText(reservation.getMotif());
        cinField.setText(reservation.getCin());
    }

    // Helper method to reset form
    private void resetForm() {
        dateReservationPicker.setValue(null);
        heureDebutComboBox.setValue(null);
        heureFinComboBox.setValue(null);
        statusComboBox.setValue(null);
        nombreParticipantsField.clear();
        motifField.clear();
        cinField.clear();
    }

    // Helper method to show alerts
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Reservation model class
    public static class Reservation {
        private int id;
        private LocalDate dateReservation;
        private LocalTime heureDebut;
        private LocalTime heureFin;
        private String status;
        private int nombreParticipants;
        private String motif;
        private String cin;

        public Reservation(int id, LocalDate dateReservation, LocalTime heureDebut, LocalTime heureFin,
                           String status, int nombreParticipants, String motif, String cin) {
            this.id = id;
            this.dateReservation = dateReservation;
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
            this.status = status;
            this.nombreParticipants = nombreParticipants;
            this.motif = motif;
            this.cin = cin;
        }

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public LocalDate getDateReservation() { return dateReservation; }
        public void setDateReservation(LocalDate dateReservation) { this.dateReservation = dateReservation; }
        public LocalTime getHeureDebut() { return heureDebut; }
        public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
        public LocalTime getHeureFin() { return heureFin; }
        public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getNombreParticipants() { return nombreParticipants; }
        public void setNombreParticipants(int nombreParticipants) { this.nombreParticipants = nombreParticipants; }
        public String getMotif() { return motif; }
        public void setMotif(String motif) { this.motif = motif; }
        public String getCin() { return cin; }
        public void setCin(String cin) { this.cin = cin; }
    }
}