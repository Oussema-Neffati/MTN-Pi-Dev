package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    // Table components
    @FXML
    private TableView<Reservation> reservationTableView;
    @FXML
    private TableColumn<Reservation, Integer> idResColumn;
    @FXML
    private TableColumn<Reservation, LocalDate> dateReservationColumn;
    @FXML
    private TableColumn<Reservation, LocalTime> heureDebutColumn;
    @FXML
    private TableColumn<Reservation, LocalTime> heureFinColumn;
    @FXML
    private TableColumn<Reservation, String> statusColumn;
    @FXML
    private TableColumn<Reservation, Integer> nombreParticipantsColumn;
    @FXML
    private TableColumn<Reservation, String> motifColumn;
    @FXML
    private TableColumn<Reservation, String> cinColumn;

    // Data list for table
    private ObservableList<Reservation> reservationList;

    // Initialize method
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

        // Initialize table columns
        idResColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateReservationColumn.setCellValueFactory(new PropertyValueFactory<>("dateReservation"));
        heureDebutColumn.setCellValueFactory(new PropertyValueFactory<>("heureDebut"));
        heureFinColumn.setCellValueFactory(new PropertyValueFactory<>("heureFin"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        nombreParticipantsColumn.setCellValueFactory(new PropertyValueFactory<>("nombreParticipants"));
        motifColumn.setCellValueFactory(new PropertyValueFactory<>("motif"));
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));

        // Initialize reservation list and set to table
        reservationList = FXCollections.observableArrayList();
        reservationTableView.setItems(reservationList);

        // Add listener for table selection
        reservationTableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        populateForm(newSelection);
                    }
                }
        );
    }

    // Note: Full-screen functionality has been removed

    // Save reservation
    @FXML
    private void saveReservation() {
        try {
            Reservation reservation = createReservationFromForm();
            reservationList.add(reservation);
            resetForm();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation enregistrée avec succès");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    // Update reservation
    @FXML
    private void updateReservation() {
        Reservation selectedReservation = reservationTableView.getSelectionModel().getSelectedItem();
        if (selectedReservation != null) {
            try {
                Reservation updatedReservation = createReservationFromForm();
                updatedReservation.setId(selectedReservation.getId());
                int index = reservationList.indexOf(selectedReservation);
                reservationList.set(index, updatedReservation);
                resetForm();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réservation modifiée avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez sélectionner une réservation à modifier");
        }
    }

    // Delete reservation
    @FXML
    private void deleteReservation() {
        Reservation selectedReservation = reservationTableView.getSelectionModel().getSelectedItem();
        if (selectedReservation != null) {
            reservationList.remove(selectedReservation);
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
        reservationTableView.getSelectionModel().clearSelection();
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