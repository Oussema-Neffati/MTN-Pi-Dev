package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import tn.esprit.models.*;
import tn.esprit.services.ServiceDocument;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserProfileController {

    @FXML
    private HBox demandBoxContainer;

    @FXML
    private Label currentUserLabel; // Ajout du Label pour le nom de l'utilisateur

    @FXML
    private Label selectedTypeLabel;

    @FXML
    private ScrollPane documentScrollPane;

    @FXML
    private FlowPane documentCardContainer;

    @FXML
    private Button retourLoginButton;

    @FXML
    private Circle statusIndicator;

    private ServiceDocument serviceDocument = new ServiceDocument();
    private List<Document> allDocuments;
    private Map<String, StackPane> typeTiles = new HashMap<>();
    private String currentDemandType;

    // Variables pour gérer le mode plein écran
    private boolean isFullScreenMode = false;
    private Document currentFullScreenDocument = null;
    private AnchorPane currentFullScreenCard = null;

    private final String[] DEMANDE_TYPES = {
            "Plans urbanisme",
            "Registre état civil",
            "Autorisation construction",
            "Règlements municipalité",
            "Légalisation d'un papier"
    };

    @FXML
    void initialize() {
        // Récupérer l'utilisateur actuel
        Utilisateur currentUser = SessionManager.getInstance().getCurrentUser();

        // Vérifier si un utilisateur est connecté et s'il est employé
        if (currentUser != null && currentUser.getRole().equals(Role.EMPLOYE)) {
            try {
                if (currentUser instanceof Employe) {
                    Employe employe = (Employe) currentUser;

                    // Mettre à jour le label en haut avec le nom complet
                    String nomComplet = (employe.getPrenom() != null ? employe.getPrenom() : "") + " "
                            + (employe.getNom() != null ? employe.getNom() : "");
                    nomComplet = nomComplet.trim();
                    if (nomComplet.isEmpty()) {
                        nomComplet = "Nom non disponible";
                    }
                    if (currentUserLabel != null) {
                        currentUserLabel.setText(nomComplet);
                    }
                } else {
                    // Si l'utilisateur n'est pas un Citoyen (mais un Employé), utiliser un nom par défaut
                    if (currentUserLabel != null) {
                        currentUserLabel.setText("Utilisateur: " + currentUser.getId());
                    }
                }
            } catch (Exception e) {
                System.out.println("Erreur: Impossible de charger les données utilisateur - " + e.getMessage());
                if (currentUserLabel != null) {
                    currentUserLabel.setText("Nom non disponible");
                }
            }
        } else {
            System.out.println("Erreur: Aucun utilisateur employé connecté ou accès refusé");
            if (currentUserLabel != null) {
                currentUserLabel.setText("Non connecté");
            }
            retourLogin(new ActionEvent());
            return;
        }

        // Configurer le FlowPane
        documentCardContainer.prefWrapLengthProperty().bind(documentScrollPane.widthProperty().subtract(20));
        documentCardContainer.setHgap(15);
        documentCardContainer.setVgap(15);
        documentScrollPane.setFitToWidth(true);

        // Afficher un message de chargement
        showLoadingMessage("Chargement des documents en cours");

        // Charger les données initiales en arrière-plan
        Thread initThread = new Thread(() -> {
            try {
                refreshDashboard();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.out.println("Erreur de Base de Données: Impossible de charger les documents - " + e.getMessage());
                    clearLoadingMessage();

                    Label errorLabel = new Label("Impossible de charger les documents.\nVérifiez la connexion à la base de données");
                    errorLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 14px;");
                    errorLabel.setWrapText(true);
                    documentCardContainer.getChildren().clear();
                    documentCardContainer.getChildren().add(errorLabel);
                });
            }
        });
        initThread.setDaemon(true);
        initThread.start();
    }

    /**
     * Affiche un message de chargement dans le conteneur de documents
     */
    private void showLoadingMessage(String message) {
        Platform.runLater(() -> {
            documentCardContainer.getChildren().clear();
            Label loadingLabel = new Label(message);
            loadingLabel.setStyle("-fx-text-fill: #3498DB; -fx-font-size: 14px;");
            documentCardContainer.getChildren().add(loadingLabel);
        });
    }

    /**
     * Efface le message de chargement
     */
    private void clearLoadingMessage() {
        Platform.runLater(() -> {
            documentCardContainer.getChildren().clear();
        });
    }

    @FXML
    void refreshDashboard() {
        Platform.runLater(() -> {
            showLoadingMessage("Actualisation des données");
        });

        try {
            allDocuments = serviceDocument.getAllDocuments();
            createDemandeTypeBoxes();

            Platform.runLater(() -> {
                documentCardContainer.getChildren().clear();
                selectedTypeLabel.setText("(Sélectionnez un type de demande)");
                currentDemandType = null;

                if (typeTiles.containsKey("Tous")) {
                    try {
                        showDocumentsForType("Tous");
                    } catch (Exception e) {
                        System.out.println("Erreur de Base de Données: Impossible de charger les documents - " + e.getMessage());
                        clearLoadingMessage();
                    }
                } else {
                    clearLoadingMessage();
                }
            });

        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.out.println("Erreur de Base de Données: Impossible de charger les documents - " + e.getMessage());
                clearLoadingMessage();

                Label errorLabel = new Label("Impossible de charger les documents.\nErreur: " + formatSqlErrorMessage(e.getMessage()));
                errorLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 14px;");
                errorLabel.setWrapText(true);
                documentCardContainer.getChildren().clear();
                documentCardContainer.getChildren().add(errorLabel);
            });
        }
    }

    /**
     * Formate le message d'erreur SQL pour le rendre plus compréhensible pour l'utilisateur
     */
    private String formatSqlErrorMessage(String errorMessage) {
        if (errorMessage.contains("unknown column") || errorMessage.contains("no such column")) {
            return "Problème de structure de la base de données. Contactez l'administrateur système";
        }
        if (errorMessage.contains("Communications link failure") ||
                errorMessage.contains("Connection refused") ||
                errorMessage.contains("No connection") ||
                errorMessage.contains("Connection timed out")) {
            return "Impossible de se connecter à la base de données. Vérifiez votre connexion réseau";
        }
        if (errorMessage.contains("foreign key constraint fails") ||
                errorMessage.contains("FOREIGN KEY constraint failed")) {
            return "Erreur de référence: L'opération ne peut pas être effectuée car elle violerait l'intégrité des données";
        }
        if (errorMessage.contains("Access denied") || errorMessage.contains("permission denied")) {
            return "Accès refusé à la base de données. Vérifiez vos identifiants";
        }
        return errorMessage;
    }

    private void createDemandeTypeBoxes() throws SQLException {
        demandBoxContainer.getChildren().clear();
        typeTiles.clear();
        demandBoxContainer.setSpacing(15);
        demandBoxContainer.setPadding(new Insets(10));

        int totalCount = allDocuments.size();
        StackPane allTile = createTypeTile("Tous", totalCount);
        typeTiles.put("Tous", allTile);
        demandBoxContainer.getChildren().add(allTile);

        for (String type : DEMANDE_TYPES) {
            long count = serviceDocument.countDocumentsByDemandeType(type);
            StackPane tile = createTypeTile(type, (int) count);
            typeTiles.put(type, tile);
            demandBoxContainer.getChildren().add(tile);
        }
    }

    private StackPane createTypeTile(String type, int count) {
        StackPane tile = new StackPane();
        tile.getStyleClass().add("document-tile");
        tile.setPrefSize(150, 120);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));

        Label typeLabel = new Label(type);
        typeLabel.getStyleClass().add("tile-title");
        typeLabel.setWrapText(true);
        typeLabel.setTextAlignment(TextAlignment.CENTER);
        typeLabel.setMaxWidth(130);

        Label countLabel = new Label(String.valueOf(count));
        countLabel.getStyleClass().add("count-label");
        countLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        content.getChildren().addAll(typeLabel, countLabel);
        tile.getChildren().add(content);

        tile.setOnMouseClicked(event -> showDocumentsForType(type));

        return tile;
    }

    private void showDocumentsForType(String type) {
        Platform.runLater(() -> {
            selectedTypeLabel.setText("Documents pour: " + type);
            currentDemandType = type;

            updateStatusIndicator(type);

            for (Map.Entry<String, StackPane> entry : typeTiles.entrySet()) {
                entry.getValue().getStyleClass().remove("selected-tile");
                if (entry.getKey().equals(type)) {
                    entry.getValue().getStyleClass().add("selected-tile");
                }
            }

            showLoadingMessage("Chargement des documents pour: " + type);
        });

        try {
            List<Document> documentsToShow;
            if ("Tous".equals(type)) {
                documentsToShow = allDocuments;
            } else {
                documentsToShow = serviceDocument.getDocumentsByDemandeType(type);
            }

            Platform.runLater(() -> {
                try {
                    displayDocuments(documentsToShow);
                } catch (SQLException e) {
                    System.out.println("Erreur de Base de Données: Impossible de charger les documents - " + e.getMessage());
                    clearLoadingMessage();
                }
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {
                System.out.println("Erreur de Base de Données: Impossible de charger les documents de type '" + type + "' - " + e.getMessage());
                clearLoadingMessage();

                String errorMessage = "Impossible de charger les documents de type '" + type + "'.\nErreur: " + formatSqlErrorMessage(e.getMessage());
                Label errorLabel = new Label(errorMessage);
                errorLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 14px;");
                errorLabel.setWrapText(true);
                documentCardContainer.getChildren().clear();
                documentCardContainer.getChildren().add(errorLabel);
            });
        }
    }

    private void updateStatusIndicator(String type) {
        if ("Plans urbanisme".equals(type)) {
            statusIndicator.setFill(Color.web("#4CAF50"));
        } else if ("Registre état civil".equals(type)) {
            statusIndicator.setFill(Color.web("#2196F3"));
        } else if ("Autorisation construction".equals(type)) {
            statusIndicator.setFill(Color.web("#FF9800"));
        } else if ("Règlements municipalité".equals(type)) {
            statusIndicator.setFill(Color.web("#9C27B0"));
        } else if ("Légalisation d'un papier".equals(type)) {
            statusIndicator.setFill(Color.web("#E91E63"));
        } else {
            statusIndicator.setFill(Color.web("#4169E1"));
        }
    }

    private void displayDocuments(List<Document> documents) throws SQLException {
        if (isFullScreenMode) {
            exitFullScreenMode();
        }

        documentCardContainer.getChildren().clear();

        if (documents.isEmpty()) {
            Label emptyLabel = new Label("Aucun document trouvé pour ce type de demande");
            emptyLabel.setFont(Font.font("System", 14));
            emptyLabel.setTextFill(Color.web("#757575"));
            documentCardContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Document document : documents) {
            try {
                Citoyen citoyen = serviceDocument.getCitoyenByDocumentId(document.getId_doc());
                AnchorPane card = createDocumentCard(citoyen, document);
                documentCardContainer.getChildren().add(card);
            } catch (SQLException e) {
                System.err.println("Error retrieving citoyen for document ID " + document.getId_doc() + ": " + e.getMessage());
            }
        }
    }

    private AnchorPane createDocumentCard(Citoyen citoyen, Document document) {
        AnchorPane card = new AnchorPane();
        card.setPrefSize(300, 200);
        card.getStyleClass().add("document-card");

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setPrefWidth(300);
        content.setAlignment(Pos.TOP_LEFT);

        Label nameLabel;
        if (citoyen != null) {
            nameLabel = new Label(citoyen.getPrenom() + " " + citoyen.getNom());
        } else {
            nameLabel = new Label("Information Citoyen Non Disponible");
        }
        nameLabel.getStyleClass().add("name-label");

        Label cinLabel;
        if (citoyen != null && citoyen.getCin() != null) {
            cinLabel = new Label("CIN: " + citoyen.getCin());
        } else {
            cinLabel = new Label("CIN: N/A");
        }
        cinLabel.getStyleClass().add("cin-label");

        Label typeLabel = new Label("Type: " + document.getType_docs());
        typeLabel.getStyleClass().add("cin-label");

        Label statusLabel = new Label("Statut: " + (document.getStatut_doc() != null ? document.getStatut_doc() : "N/A"));
        statusLabel.getStyleClass().add("status-label");

        if (document.getStatut_doc() != null) {
            String status = document.getStatut_doc().toLowerCase();
            if (status.contains("traitement")) {
                statusLabel.getStyleClass().add("status-pending");
            } else if (status.contains("validé") || status.contains("approuvé")) {
                statusLabel.getStyleClass().add("status-approved");
            } else if (status.contains("rejeté") || status.contains("refusé")) {
                statusLabel.getStyleClass().add("status-rejected");
            }
        }

        String dateStr = "N/A";
        if (document.getDate_emission_doc() != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            dateStr = sdf.format(document.getDate_emission_doc());
        }
        Label dateLabel = new Label("Date: " + dateStr);
        dateLabel.getStyleClass().add("cin-label");

        Region spacer = new Region();
        spacer.setPrefHeight(10);

        HBox archiveControlBox = new HBox(10);
        archiveControlBox.setAlignment(Pos.CENTER_LEFT);

        Label archiveLabel = new Label(document.isArchive() ? "Archivé" : "Non archivé");
        archiveLabel.getStyleClass().addAll("archive-label",
                document.isArchive() ? "archive-true" : "archive-false");

        ToggleButton archiveSwitch = new ToggleButton();
        archiveSwitch.getStyleClass().add("archive-switch");
        archiveSwitch.setSelected(document.isArchive());

        archiveSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            archiveLabel.setText(newVal ? "Archivé" : "Non archivé");
            archiveLabel.getStyleClass().removeAll("archive-true", "archive-false");
            archiveLabel.getStyleClass().add(newVal ? "archive-true" : "archive-false");

            Document updatedDoc = new Document();
            updatedDoc.setId_doc(document.getId_doc());
            updatedDoc.setArchive(newVal);
            toggleArchiveStatus(updatedDoc);
        });

        archiveControlBox.getChildren().addAll(archiveLabel, archiveSwitch);

        Button fullScreenButton = new Button("Agrandir");
        fullScreenButton.getStyleClass().add("action-button");
        fullScreenButton.setOnAction(e -> toggleFullScreenMode(card, document, citoyen));

        content.getChildren().addAll(nameLabel, cinLabel, typeLabel, statusLabel, dateLabel, spacer, archiveControlBox, fullScreenButton);

        AnchorPane.setTopAnchor(content, 0.0);
        AnchorPane.setLeftAnchor(content, 0.0);
        AnchorPane.setRightAnchor(content, 0.0);
        AnchorPane.setBottomAnchor(content, 0.0);
        card.getChildren().add(content);

        return card;
    }

    private void toggleFullScreenMode(AnchorPane card, Document document, Citoyen citoyen) {
        if (isFullScreenMode && card == currentFullScreenCard) {
            exitFullScreenMode();
        } else {
            enterFullScreenMode(card, document, citoyen);
        }
    }

    private void enterFullScreenMode(AnchorPane card, Document document, Citoyen citoyen) {
        isFullScreenMode = true;
        currentFullScreenDocument = document;
        currentFullScreenCard = card;

        for (Node node : documentCardContainer.getChildren()) {
            if (node != card) {
                node.setVisible(false);
                node.setManaged(false);
            }
        }

        card.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        VBox content = (VBox) card.getChildren().get(0);
        Button fullScreenButton = (Button) content.getChildren().get(content.getChildren().size() - 1);
        fullScreenButton.setText("Réduire");

        Label detailsLabel = new Label("Détails Supplémentaires");
        detailsLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        detailsLabel.setStyle("-fx-padding: 10 0 5 0;");

        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefHeight(150);
        detailsArea.setWrapText(true);

        StringBuilder details = new StringBuilder();
        details.append("Document ID: ").append(document.getId_doc()).append("\n");
        details.append("Type: ").append(document.getType_docs()).append("\n");
        details.append("Date d'émission: ").append(document.getDate_emission_doc() != null ?
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(document.getDate_emission_doc()) :
                "Non disponible").append("\n");
        details.append("Statut: ").append(document.getStatut_doc() != null ? document.getStatut_doc() : "Non défini").append("\n");
        details.append("Archivé: ").append(document.isArchive() ? "Oui" : "Non").append("\n");

        if (citoyen != null) {
            details.append("\nInformations du Citoyen:\n");
            details.append("Nom complet: ").append(citoyen.getNom()).append(" ").append(citoyen.getPrenom()).append("\n");
            details.append("CIN: ").append(citoyen.getCin() != null ? citoyen.getCin() : "Non disponible").append("\n");
            if (citoyen.getEmail() != null) details.append("Email: ").append(citoyen.getEmail()).append("\n");
            if (citoyen.getTelephone() != null) details.append("Téléphone: ").append(citoyen.getTelephone()).append("\n");
        }

        detailsArea.setText(details.toString());

        int buttonIndex = content.getChildren().size() - 1;
        content.getChildren().add(buttonIndex, detailsLabel);
        content.getChildren().add(buttonIndex + 1, detailsArea);

        card.getStyleClass().add("full-screen-card");
    }

    private void exitFullScreenMode() {
        if (!isFullScreenMode || currentFullScreenCard == null) return;

        isFullScreenMode = false;

        for (Node node : documentCardContainer.getChildren()) {
            node.setVisible(true);
            node.setManaged(true);
        }

        currentFullScreenCard.setPrefSize(300, 200);
        currentFullScreenCard.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        VBox content = (VBox) currentFullScreenCard.getChildren().get(0);
        Button fullScreenButton = (Button) content.getChildren().get(content.getChildren().size() - 1);
        fullScreenButton.setText("Agrandir");

        content.getChildren().removeIf(node ->
                (node instanceof Label && ((Label) node).getText().equals("Détails Supplémentaires")) ||
                        (node instanceof TextArea));

        currentFullScreenCard.getStyleClass().remove("full-screen-card");

        currentFullScreenDocument = null;
        currentFullScreenCard = null;
    }

    private void toggleArchiveStatus(Document document) {
        Platform.runLater(() -> {
            documentCardContainer.setDisable(true);
            showLoadingMessage("Mise à jour du statut d'archivage");
        });

        Thread updateThread = new Thread(() -> {
            try {
                boolean newArchiveStatus = document.isArchive();
                serviceDocument.updateArchiveStatus(document.getId_doc(), newArchiveStatus);

                Platform.runLater(() -> {
                    documentCardContainer.setDisable(false);
                    String message = newArchiveStatus ? "Document archivé avec succès" : "Document désarchivé avec succès";
                    System.out.println(message);

                    if (currentDemandType != null) {
                        try {
                            showDocumentsForType(currentDemandType);
                        } catch (Exception e) {
                            System.out.println("Erreur de Base de Données: Impossible de charger les documents - " + e.getMessage());
                        }
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    documentCardContainer.setDisable(false);
                    System.out.println("Erreur de Base de Données: Impossible de modifier l'état d'archivage - " + e.getMessage());

                    String errorMessage = "Impossible de modifier l'état d'archivage: " + formatSqlErrorMessage(e.getMessage());
                    Label errorLabel = new Label(errorMessage);
                    errorLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-size: 14px;");
                    errorLabel.setWrapText(true);
                    documentCardContainer.getChildren().clear();
                    documentCardContainer.getChildren().add(errorLabel);

                    try {
                        if (currentDemandType != null) {
                            showDocumentsForType(currentDemandType);
                        }
                    } catch (Exception ex) {
                        System.out.println("Erreur secondaire: " + ex.getMessage());
                    }
                });
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    @FXML
    void retourLogin(ActionEvent event) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Déconnexion");

        VBox dialogVbox = new VBox(20);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setAlignment(Pos.CENTER);

        Label confirmMessage = new Label("Voulez-vous vraiment vous déconnecter ?");
        confirmMessage.setFont(Font.font("System", 14));

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button okButton = new Button("OK");
        Button cancelButton = new Button("Annuler");

        buttonBox.getChildren().addAll(okButton, cancelButton);
        dialogVbox.getChildren().addAll(confirmMessage, buttonBox);

        Scene dialogScene = new Scene(dialogVbox, 300, 150);
        dialogStage.setScene(dialogScene);

        okButton.setOnAction(e -> {
            SessionManager.getInstance().clearSession();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/LoginView.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) demandBoxContainer.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Connexion");
                stage.show();
                dialogStage.close();
            } catch (IOException ex) {
                System.out.println("Erreur: Impossible de charger l'écran de connexion");
                dialogStage.close();
            }
        });

        cancelButton.setOnAction(e -> dialogStage.close());

        dialogStage.showAndWait();
    }
}