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
import javafx.scene.Node;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ressourceService = new ServiceRessource();
        resourceHistory = new LinkedHashMap<>(); // Use LinkedHashMap to maintain insertion order

        // Initial setup
        setupInitialData();
        setupEventHandlers();
        setupChartAnimations();
    }

    private void setupInitialData() {
        loadRessources();
        initializeFilters();
        updateStatistics();
        updateCharts();
        displayResourceCards();
    }

    private void setupEventHandlers() {
        // Add real-time search functionality
        txtFilterSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            displayResourceCards();
        });

        // Add category filter change listener
        cmbFilterCategorie.setOnAction(event -> displayResourceCards());

        // Add availability filter change listener
        chkFilterDisponible.setOnAction(event -> displayResourceCards());
    }

    private void setupChartAnimations() {
        // Add hover effect for pie chart slices
        pieChartCategories.getData().forEach(data -> {
            Node node = data.getNode();
            node.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
                st.setToX(1.1);
                st.setToY(1.1);
                st.play();
            });
            node.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
                st.setToX(1);
                st.setToY(1);
                st.play();
            });
        });
    }

    private void loadRessources() {
        try {
            ressources = ressourceService.readAll();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les ressources", e.getMessage());
        }
    }

    private void initializeFilters() {
        ObservableList<String> categories = FXCollections.observableArrayList();
        categories.add("Toutes les catégories");

        if (ressources != null && !ressources.isEmpty()) {
            categories.addAll(
                ressources.stream()
                    .map(Ressource::getCategorie)
                    .distinct()
                    .sorted()
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

        Map<String, Long> countByCategory = ressources.stream()
                .collect(Collectors.groupingBy(
                        Ressource::getCategorie,
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        countByCategory.forEach((category, count) -> {
            PieChart.Data slice = new PieChart.Data(category + " (" + count + ")", count);
            pieChartData.add(slice);
        });

        pieChartCategories.setData(pieChartData);
        setupChartAnimations();
    }

    private void updateLineChart() {
        if (ressources == null) return;

        // Update history with current count
        String currentTime = LocalDateTime.now().format(timeFormatter);
        resourceHistory.put(currentTime, ressources.size());

        // Keep only last 10 entries
        if (resourceHistory.size() > 10) {
            String firstKey = resourceHistory.keySet().iterator().next();
            resourceHistory.remove(firstKey);
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre de ressources");

        resourceHistory.forEach((time, count) -> 
            series.getData().add(new XYChart.Data<>(time, count))
        );

        lineChartEvolution.getData().clear();
        lineChartEvolution.getData().add(series);
    }

    private void displayResourceCards() {
        if (ressources == null) return;

        resourceCardsContainer.getChildren().clear();

        ressources.stream()
                .filter(this::passesFilters)
                .forEach(ressource -> {
                    try {
                        VBox card = createResourceCard(ressource);
                        resourceCardsContainer.getChildren().add(card);
                    } catch (Exception e) {
                        System.err.println("Error creating card for resource: " + ressource.getNom());
                        e.printStackTrace();
                    }
                });
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
        return searchText.isEmpty() ||
               ressource.getNom().toLowerCase().contains(searchText) ||
               ressource.getDescription().toLowerCase().contains(searchText);
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

        // Description
        Text txtDescription = new Text(ressource.getDescription());
        txtDescription.setWrappingWidth(270);

        // Buttons container
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));

        // Edit button
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().addAll("btn", "btn-edit");
        editButton.setOnAction(e -> handleEditResource(ressource));

        // Delete button
        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("btn", "btn-delete");
        deleteButton.setOnAction(e -> handleDeleteResource(ressource));

        buttonsBox.getChildren().addAll(editButton, deleteButton);

        // Add all elements to card
        card.getChildren().addAll(
                lblName,
                categoryBox,
                lblStatus,
                new Separator(),
                detailsBox,
                txtDescription,
                buttonsBox
        );

        // Add hover effect
        setupCardHoverEffect(card);

        return card;
    }

    private void setupCardHoverEffect(VBox card) {
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.03);
            st.setToY(1.03);
            st.play();
        });

        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1);
            st.setToY(1);
            st.play();
        });
    }

    private void handleEditResource(Ressource ressource) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Ressource.fxml"));
            Parent root = loader.load();

            RessourceController controller = loader.getController();
            controller.loadRessource(ressource.getId());

            Scene scene = btnRetour.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'interface de modification", e.getMessage());
        }
    }

    private void handleDeleteResource(Ressource ressource) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Supprimer la ressource");
        confirmDialog.setContentText("Êtes-vous sûr de vouloir supprimer la ressource \"" + ressource.getNom() + "\" ?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ressourceService.delete(ressource.getId());
                    refreshData();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Suppression réussie", 
                            "La ressource a été supprimée avec succès.");
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de suppression", 
                            "Impossible de supprimer la ressource: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleApplyFilter(ActionEvent event) {
        displayResourceCards();
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Ressource.fxml"));
            Parent root = loader.load();
            Scene scene = btnRetour.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de retourner à l'écran précédent", e.getMessage());
        }
    }

    public void refreshData() {
        setupInitialData();
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}