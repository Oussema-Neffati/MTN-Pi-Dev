package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import tn.esprit.models.Ressource;
import tn.esprit.services.ServiceRessource;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RessourceVisualizationController implements Initializable {

    @FXML
    private PieChart pieChartCategories;

    @FXML
    private LineChart<String, Number> lineChartEvolution;

    @FXML
    private FlowPane resourceCardsContainer;

    @FXML
    private ComboBox<String> cmbFilterCategorie;

    @FXML
    private CheckBox chkFilterDisponible;

    @FXML
    private TextField txtFilterSearch;

    @FXML
    private Button btnApplyFilter;

    @FXML
    private Button btnRetour;

    @FXML
    private Label lblTotalRessources;

    @FXML
    private Label lblRessourcesDisponibles;

    @FXML
    private Label lblTarifMoyen;

    private ServiceRessource ressourceService;
    private List<Ressource> ressources;
    private Map<String, Integer> resourceHistory;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ressourceService = new ServiceRessource();
        resourceHistory = new HashMap<>();

        // Load all resources
        loadRessources();

        // Initialize filter categories
        initializeFilters();

        // Update UI
        updateStatistics();
        updateCharts();
        displayResourceCards();
    }

    private void loadRessources() {
        try {
            ressources = ressourceService.readAll();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les ressources", e.getMessage());
        }
    }

    private void initializeFilters() {
        // Get all unique categories
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add("Toutes les catégories");

        if (ressources != null && !ressources.isEmpty()) {
            categories.addAll(
                    ressources.stream()
                            .map(Ressource::getCategorie)
                            .distinct()
                            .collect(Collectors.toList())
            );
        }

        cmbFilterCategorie.setItems(categories);
        cmbFilterCategorie.setValue("Toutes les catégories");
    }

    private void updateStatistics() {
        if (ressources == null) return;

        int totalRessources = ressources.size();
        long ressourcesDisponibles = ressources.stream()
                .filter(Ressource::isDisponible)
                .count();

        double tarifMoyen = ressources.stream()
                .mapToDouble(Ressource::getTarifHoraire)
                .average()
                .orElse(0.0);

        lblTotalRessources.setText(String.valueOf(totalRessources));
        lblRessourcesDisponibles.setText(String.valueOf(ressourcesDisponibles));
        lblTarifMoyen.setText(String.format("%.2f €/h", tarifMoyen));
    }

    private void updateCharts() {
        updatePieChart();
        updateLineChart();
    }

    private void updatePieChart() {
        if (ressources == null) return;

        // Count resources by category
        Map<String, Long> countByCategory = ressources.stream()
                .collect(Collectors.groupingBy(
                        Ressource::getCategorie,
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        countByCategory.forEach((category, count) -> {
            pieChartData.add(new PieChart.Data(category + " (" + count + ")", count));
        });

        pieChartCategories.setData(pieChartData);
    }

    private void updateLineChart() {
        // Update resource history with current count
        if (ressources != null) {
            // For the demo, we'll just add the current timestamp and count
            String timestamp = String.valueOf(System.currentTimeMillis());
            resourceHistory.put(timestamp, ressources.size());
        }

        // Create series for the chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de ressources");

        // Only use the last 10 entries to keep the chart clean
        resourceHistory.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(10)
                .forEach(entry -> {
                    // Format the timestamp for display
                    String label = formatTimestamp(entry.getKey());
                    series.getData().add(new XYChart.Data<>(label, entry.getValue()));
                });

        lineChartEvolution.getData().clear();
        lineChartEvolution.getData().add(series);
    }

    private String formatTimestamp(String timestamp) {
        // In a real application, you would convert the timestamp to a readable date
        // For simplicity, we'll just use a shortened version of the timestamp
        return timestamp.substring(timestamp.length() - 5);
    }

    private void displayResourceCards() {
        if (ressources == null) return;

        resourceCardsContainer.getChildren().clear();

        for (Ressource ressource : ressources) {
            // Apply filters
            if (!passesFilters(ressource)) continue;

            try {
                // Create a card for each resource
                VBox card = createResourceCard(ressource);
                resourceCardsContainer.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("Error creating card for resource: " + ressource.getNom());
                e.printStackTrace();
            }
        }
    }

    private boolean passesFilters(Ressource ressource) {
        // Category filter
        String selectedCategory = cmbFilterCategorie.getValue();
        if (!"Toutes les catégories".equals(selectedCategory) &&
                !ressource.getCategorie().equals(selectedCategory)) {
            return false;
        }

        // Availability filter
        if (chkFilterDisponible.isSelected() && !ressource.isDisponible()) {
            return false;
        }

        // Search filter
        String searchText = txtFilterSearch.getText().toLowerCase();
        if (!searchText.isEmpty() &&
                !ressource.getNom().toLowerCase().contains(searchText) &&
                !ressource.getDescription().toLowerCase().contains(searchText)) {
            return false;
        }

        return true;
    }

    private VBox createResourceCard(Ressource ressource) {
        VBox card = new VBox();
        card.getStyleClass().add("resource-card");
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(200);
        card.setSpacing(10);
        card.setPadding(new Insets(15));

        // Resource name
        Label lblName = new Label(ressource.getNom());
        lblName.getStyleClass().add("card-title");

        // Category with badge
        HBox categoryBox = new HBox();
        categoryBox.setSpacing(5);
        Label badgeCategory = new Label(ressource.getCategorie());
        badgeCategory.getStyleClass().add("category-badge");
        categoryBox.getChildren().add(badgeCategory);

        // Available status
        Label lblStatus = new Label(ressource.isDisponible() ? "Disponible" : "Non disponible");
        lblStatus.getStyleClass().add(ressource.isDisponible() ? "status-available" : "status-unavailable");

        // Details
        VBox detailsBox = new VBox();
        detailsBox.setSpacing(5);

        Label lblCapacite = new Label("Capacité: " + ressource.getCapacite());
        Label lblTarif = new Label("Tarif: " + ressource.getTarifHoraire() + " €/h");
        Label lblHoraires = new Label("Horaires: " + ressource.getHoraireOuverture() + " - " + ressource.getHoraireFermeture());

        detailsBox.getChildren().addAll(lblCapacite, lblTarif, lblHoraires);

        // Description (truncated if too long)
        Text txtDescription = new Text(ressource.getDescription());
        txtDescription.setWrappingWidth(270);

        // Add all elements to card
        card.getChildren().addAll(
                lblName,
                categoryBox,
                lblStatus,
                new Separator(),
                detailsBox,
                txtDescription
        );

        return card;
    }

    @FXML
    private void handleApplyFilter(ActionEvent event) {
        displayResourceCards();
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            // Go back to the main ressource screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Ressource.fxml"));
            Parent root = loader.load();

            // Replace current scene
            Scene scene = btnRetour.getScene();
            scene.setRoot(root);

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à l'écran précédent", e.getMessage());
        }
    }

    // Method that can be called from RessourceController after adding a new resource
    public void refreshData() {
        loadRessources();
        updateStatistics();
        updateCharts();
        displayResourceCards();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}