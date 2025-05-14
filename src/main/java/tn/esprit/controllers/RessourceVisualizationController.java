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
import tn.esprit.services.QrCodeService;
import javafx.scene.Node;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Modality;

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
    private QrCodeService qrCodeService;
    private List<Ressource> ressources;
    private Map<String, Integer> resourceHistory;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ressourceService = new ServiceRessource();
        qrCodeService = new QrCodeService();
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
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPrefWidth(300);

        // Resource name
        Label nameLabel = new Label(ressource.getNom());
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        // Resource details
        VBox details = new VBox(5);
        details.getChildren().addAll(
            new Label("Catégorie: " + ressource.getCategorie()),
            new Label("Capacité: " + ressource.getCapacite()),
            new Label("Tarif: " + String.format("%.2f €/h", ressource.getTarifHoraire())),
            new Label("Horaires: " + ressource.getHoraireOuverture() + " - " + ressource.getHoraireFermeture()),
            new Label("Disponible: " + (ressource.isDisponible() ? "Oui" : "Non"))
        );

        // Description with scroll capability
        TextArea description = new TextArea(ressource.getDescription());
        description.setWrapText(true);
        description.setEditable(false);
        description.setPrefRowCount(3);
        description.setStyle("-fx-background-color: transparent;");

        // QR Code
        ImageView qrCodeView = new ImageView();
        qrCodeView.setFitWidth(150);
        qrCodeView.setFitHeight(150);
        qrCodeView.setPreserveRatio(true);

        try {
            Image qrCodeImage = qrCodeService.getQrCodeForResource(ressource);
            qrCodeView.setImage(qrCodeImage);
        } catch (Exception e) {
            System.err.println("Error generating QR code for resource: " + ressource.getNom());
            e.printStackTrace();
        }

        // Make QR code clickable to show in larger view
        qrCodeView.setOnMouseClicked(event -> {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("QR Code - " + ressource.getNom());

            ImageView largeQrView = new ImageView(qrCodeView.getImage());
            largeQrView.setFitWidth(300);
            largeQrView.setFitHeight(300);
            largeQrView.setPreserveRatio(true);

            VBox dialogVbox = new VBox(10);
            dialogVbox.setPadding(new Insets(15));
            dialogVbox.getChildren().addAll(
                new Label("Scannez ce QR code pour accéder aux détails de la ressource"),
                largeQrView
            );

            Scene dialogScene = new Scene(dialogVbox);
            dialog.setScene(dialogScene);
            dialog.show();
        });

        // Action buttons
        Button editButton = new Button("Modifier");
        editButton.setOnAction(e -> handleEditResource(ressource));

        Button deleteButton = new Button("Supprimer");
        deleteButton.setOnAction(e -> handleDeleteResource(ressource));

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(editButton, deleteButton);

        // Add all components to card
        card.getChildren().addAll(
            nameLabel,
            details,
            description,
            qrCodeView,
            buttons
        );

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