package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.esprit.models.Citoyen;
import tn.esprit.models.Document;
import tn.esprit.models.Role;
import tn.esprit.services.ServiceDocument;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EmployeeMunDashboardController {

    @FXML
    private HBox demandBoxContainer;

    @FXML
    private Label selectedTypeLabel;

    @FXML
    private ScrollPane documentScrollPane;

    @FXML
    private FlowPane documentCardContainer;

    @FXML
    private Button retourLoginButton;

    private ServiceDocument serviceDocument = new ServiceDocument();
    private List<Document> allDocuments;
    private Map<String, Button> typeButtons = new HashMap<>();
    private String currentDemandType;

    private final String[] DEMANDE_TYPES = {
            "Plans urbanisme",
            "Registre état civil",
            "Autorisation construction",
            "Règlements municipalité",
            "Légalisation d'un papier"
    };

    @FXML
    void initialize() {
        // Vérifier que l'utilisateur est un employé
        if (!SessionManager.getInstance().getCurrentUser().getRole().equals(Role.EMPLOYE)) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Vous n'avez pas les droits d'accès à cette interface.");
            retourLogin(new ActionEvent());
            return;
        }

        // Configurer le FlowPane
        documentCardContainer.prefWrapLengthProperty().bind(documentScrollPane.widthProperty().subtract(20));
        documentCardContainer.setHgap(15);
        documentCardContainer.setVgap(15);
        documentScrollPane.setFitToWidth(true);

        // Charger les données initiales
        refreshDashboard();
    }

    @FXML
    void refreshDashboard() {
        try {
            // Charger tous les documents
            allDocuments = serviceDocument.getAllDocuments();

            // Afficher les boîtes de types de demandes
            createDemandeTypeBoxes();

            // Effacer les documents affichés
            documentCardContainer.getChildren().clear();

            // Réinitialiser le label de type sélectionné
            selectedTypeLabel.setText("(Sélectionnez un type de demande)");
            currentDemandType = null;

            // Sélectionner "Tous" par défaut
            if (typeButtons.containsKey("Tous")) {
                showDocumentsForType("Tous");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les documents: " + e.getMessage());
        }
    }

    private void createDemandeTypeBoxes() throws SQLException {
        demandBoxContainer.getChildren().clear();
        typeButtons.clear();
        demandBoxContainer.setSpacing(20);
        demandBoxContainer.setPadding(new Insets(10));

        // Ajouter le bouton "Tous"
        int totalCount = allDocuments.size();
        Button allButton = createTypeButton("Tous", totalCount);
        typeButtons.put("Tous", allButton);
        demandBoxContainer.getChildren().add(allButton);

        // Créer un bouton pour chaque type de demande
        for (String type : DEMANDE_TYPES) {
            long count = serviceDocument.countDocumentsByDemandeType(type);
            Button button = createTypeButton(type, (int) count);
            typeButtons.put(type, button);
            demandBoxContainer.getChildren().add(button);
        }
    }

    private Button createTypeButton(String type, int count) {
        Button button = new Button();
        button.getStyleClass().add("demand-type-button");
        button.setPrefSize(180, 120);
        button.setAlignment(Pos.CENTER);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);

        Label typeLabel = new Label(type);
        typeLabel.getStyleClass().add("type-label");
        typeLabel.setWrapText(true);
        typeLabel.setMaxWidth(160);
        typeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("count-label");
        countLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        content.getChildren().addAll(typeLabel, countLabel);
        button.setGraphic(content);

        // Action lors du clic sur le bouton
        button.setOnAction(event -> showDocumentsForType(type));

        return button;
    }

    private void showDocumentsForType(String type) {
        try {
            // Mettre à jour l'étiquette du type sélectionné
            selectedTypeLabel.setText("Documents pour: " + type);
            currentDemandType = type;

            // Mettre en évidence le bouton sélectionné
            for (Map.Entry<String, Button> entry : typeButtons.entrySet()) {
                entry.getValue().getStyleClass().remove("selected-type-button");
                if (entry.getKey().equals(type)) {
                    entry.getValue().getStyleClass().add("selected-type-button");
                }
            }

            // Récupérer les documents
            List<Document> documentsToShow;
            if ("Tous".equals(type)) {
                documentsToShow = allDocuments;
            } else {
                documentsToShow = serviceDocument.getDocumentsByDemandeType(type);
            }

            // Afficher les documents
            displayDocuments(documentsToShow);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les documents: " + e.getMessage());
        }
    }

    private void displayDocuments(List<Document> documents) throws SQLException {
        documentCardContainer.getChildren().clear();

        if (documents.isEmpty()) {
            Label emptyLabel = new Label("Aucun document trouvé pour ce type de demande.");
            emptyLabel.setFont(Font.font("System", 14));
            emptyLabel.setTextFill(Color.web("#757575"));
            documentCardContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Document document : documents) {
            Citoyen citoyen = serviceDocument.getCitoyenByDocumentId(document.getId_doc());
            if (citoyen != null) {
                AnchorPane card = createDocumentCard(citoyen, document);
                documentCardContainer.getChildren().add(card);
            }
        }
    }

    private AnchorPane createDocumentCard(Citoyen citoyen, Document document) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(300, 180);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; " +
                "-fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        VBox content = new VBox(8);
        content.setPadding(new Insets(15));
        content.setPrefSize(300, 180);

        // Nom et prénom
        Label nameLabel = new Label(citoyen.getPrenom() + " " + citoyen.getNom());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#333333"));

        // CIN
        Label cinLabel = new Label("CIN: " + (citoyen.getCin() != null ? citoyen.getCin() : "N/A"));
        cinLabel.setFont(Font.font("System", 12));
        cinLabel.setTextFill(Color.web("#757575"));

        // Statut du document
        Label statusLabel = new Label("Statut: " + (document.getStatut_doc() != null ? document.getStatut_doc() : "N/A"));
        statusLabel.setFont(Font.font("System", 12));
        statusLabel.setTextFill(Color.web("#757575"));

        // État d'archivage
        Label archiveLabel = new Label("État: " + (document.isArchive() ? "Archivé" : "Non archivé"));
        archiveLabel.setFont(Font.font("System", 12));
        archiveLabel.setTextFill(document.isArchive() ? Color.web("#FF6B6B") : Color.web("#51CF66"));

        // Bouton pour archiver/désarchiver
        Button archiveButton = new Button(document.isArchive() ? "Désarchiver" : "Archiver");
        archiveButton.setPrefSize(100, 30);
        archiveButton.setStyle(document.isArchive() ?
                "-fx-background-color: #51CF66; -fx-text-fill: white; -fx-background-radius: 8;" :
                "-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-background-radius: 8;");
        archiveButton.setFont(Font.font("System", FontWeight.BOLD, 11));
        archiveButton.setOnAction(event -> toggleArchiveStatus(document));

        content.getChildren().addAll(nameLabel, cinLabel, statusLabel, archiveLabel, archiveButton);
        card.getChildren().add(content);

        return card;
    }

    private void toggleArchiveStatus(Document document) {
        try {
            boolean newArchiveStatus = !document.isArchive();
            serviceDocument.updateArchiveStatus(document.getId_doc(), newArchiveStatus);
            document.setArchive(newArchiveStatus);

            String message = newArchiveStatus ? "Document archivé avec succès." : "Document désarchivé avec succès.";
            showAlert(Alert.AlertType.INFORMATION, "Succès", message);

            // Rafraîchir les cartes pour le type actuel
            if (currentDemandType != null) {
                showDocumentsForType(currentDemandType);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de modifier l'état d'archivage: " + e.getMessage());
        }
    }

    @FXML
    void retourLogin(ActionEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment vous déconnecter ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Déconnecter l'utilisateur
            SessionManager.getInstance().clearSession();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root);
                Stage stage = (Stage) demandBoxContainer.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Connexion");
                stage.show();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de charger l'écran de connexion.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}