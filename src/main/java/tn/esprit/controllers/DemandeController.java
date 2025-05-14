package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import tn.esprit.models.Demande;
import tn.esprit.models.Document;
import tn.esprit.models.Citoyen;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ServiceDemande;
import tn.esprit.services.ServiceDocument;
import tn.esprit.services.StripePayment;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DemandeController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(DemandeController.class.getName());

    @FXML
    private TextField idUserField;

    @FXML
    private TextField nomField;

    @FXML
    private ComboBox<String> typeDocumentCombo;

    @FXML
    private TextField adresseField;

    @FXML
    private TextField prixField;

    @FXML
    private Button mapButton;

    @FXML
    private Label currentUserLabel;

    private Map mapController;
    private ServiceDemande serviceDemande;
    private ServiceDocument serviceDocument;
    private StripePayment stripePaymentService;
    private Citoyen currentCitoyen;
    private HashMap<String, Double> documentPrices;
    private boolean userFieldsPrefilled = false;
    private String paymentSessionId;
    private float totalAmount;
    private boolean paymentCompleted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            LOGGER.info("Initializing DemandeController...");

            // Initialize services
            LOGGER.info("Initializing services...");
            initializeServices();
            LOGGER.info("Services initialized successfully.");

            // Initialize UI components
            LOGGER.info("Initializing UI components...");
            initializeUIComponents();
            LOGGER.info("UI components initialized successfully.");

            // Initialize document types
            LOGGER.info("Initializing document types...");
            initializeDocumentTypes();
            LOGGER.info("Document types initialized successfully.");

            // Initialize map controller
            LOGGER.info("Initializing map controller...");
            initializeMapController();
            LOGGER.info("Map controller initialized successfully.");

            // Set up event handlers
            LOGGER.info("Setting up event handlers...");
            setupMapButton();
            setupFieldValidations();
            LOGGER.info("Event handlers set up successfully.");

            // Initialize user data
            LOGGER.info("Initializing user data...");
            initializeUserData();
            LOGGER.info("User data initialized successfully.");

            LOGGER.info("DemandeController initialized successfully.");
        } catch (Exception e) {
            e.printStackTrace(); // Ensure stack trace is printed
            String errorMessage = e.getMessage() != null ? e.getMessage() : "No specific error message available";
            LOGGER.log(Level.SEVERE, "Unexpected error during initialization: " + errorMessage, e);
            showAlert("Erreur", "Erreur inattendue lors de l'initialisation: " + errorMessage);
            throw new RuntimeException("Initialization failed", e); // Rethrow to stop further execution
        }
    }

    private void initializeServices() {
        try {
            serviceDemande = new ServiceDemande();
            serviceDocument = new ServiceDocument();
            stripePaymentService = new StripePayment();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize services: " + e.getMessage(), e);
            throw new RuntimeException("Service initialization failed", e);
        }
    }

    private void initializeUIComponents() {
        prixField.setEditable(false);
        adresseField.setEditable(false);
    }

    private void initializeDocumentTypes() {
        documentPrices = new HashMap<>();
        documentPrices.put("Plans urbanisme", 75.00);
        documentPrices.put("Registre état civil", 25.00);
        documentPrices.put("Autorisation construction", 150.00);
        documentPrices.put("Règlements municipalité", 50.00);
        documentPrices.put("Légalisation d'un papier", 15.00);

        ObservableList<String> documentTypes = FXCollections.observableArrayList(documentPrices.keySet());
        typeDocumentCombo.setItems(documentTypes);
        typeDocumentCombo.valueProperty().addListener((obs, oldVal, newVal) -> handleTypeDocumentChange());
    }

    private void initializeMapController() {
        try {
            mapController = new Map();
            mapController.setLinkedAddressField(adresseField);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize Map controller: " + e.getMessage(), e);
            showAlert("Erreur", "Erreur lors de l'initialisation de la carte: " + e.getMessage());
        }
    }

    private void setupMapButton() {
        mapButton.setOnAction(event -> handleOpenMap());
    }

    private void setupFieldValidations() {
        // CIN Validation
        idUserField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!idUserField.isEditable()) {
                event.consume();
                return;
            }
            String character = event.getCharacter();
            if (!character.matches("[0-9]")) {
                event.consume();
                return;
            }
            if (idUserField.getText().isEmpty() && !character.matches("[01]")) {
                event.consume();
                return;
            }
            if (idUserField.getText().length() >= 8) {
                event.consume();
            }
        });

        idUserField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && idUserField.isEditable()) {
                validateCIN();
            }
        });

        // Nom Validation
        nomField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!nomField.isEditable()) {
                event.consume();
                return;
            }
            String character = event.getCharacter();
            if (!character.matches("[a-zA-Z\\s]")) {
                event.consume();
            }
        });

        nomField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && nomField.isEditable()) {
                validateNom();
            }
        });

        // Adresse Validation (ensure it's not empty when submitting)
        adresseField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validateAdresse();
            }
        });

        // Style for read-only fields
        String readOnlyStyle = "-fx-background-color: #f0f0f0; -fx-opacity: 0.85;";
        prixField.setStyle(readOnlyStyle);
        adresseField.setStyle(readOnlyStyle);
    }

    public void initializeUserData() {
        try {
            LOGGER.info("Fetching current user from SessionManager...");
            Utilisateur user = SessionManager.getInstance().getCurrentUser();
            LOGGER.info("Current User: " + (user != null ? user.toString() : "null"));
            LOGGER.info("SessionManager.isCitoyen(): " + SessionManager.getInstance().isCitoyen());

            if (user != null && SessionManager.getInstance().isCitoyen()) {
                currentCitoyen = (Citoyen) user; // Safe cast since isCitoyen() already checks the type
                LOGGER.info("Current Citoyen: " + currentCitoyen.toString());

                String cin = currentCitoyen.getCin() != null && !currentCitoyen.getCin().isEmpty()
                        ? currentCitoyen.getCin()
                        : String.valueOf(currentCitoyen.getId());
                idUserField.setText(cin);
                idUserField.setEditable(false);
                idUserField.setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 0.85;");

                String nomComplet = (currentCitoyen.getPrenom() != null ? currentCitoyen.getPrenom() : "") + " "
                        + (currentCitoyen.getNom() != null ? currentCitoyen.getNom() : "");
                nomComplet = nomComplet.trim();
                if (nomComplet.isEmpty()) {
                    nomComplet = "Nom non disponible";
                }
                nomField.setText(nomComplet);
                nomField.setEditable(false);
                nomField.setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 0.85;");

                if (currentUserLabel != null) {
                    currentUserLabel.setText(nomComplet);
                }

                String adresse = currentCitoyen.getAdresse() != null ? currentCitoyen.getAdresse() : "";
                if (!adresse.isEmpty()) {
                    adresseField.setText(adresse);
                }

                userFieldsPrefilled = true;
            } else {
                String errorMessage = user == null ? "No user is logged in." : "Logged-in user is not a Citoyen (Role: " + user.getRole() + ").";
                LOGGER.warning(errorMessage);
                showAlert("Erreur", errorMessage + " Vérifiez votre connexion ou votre type de compte.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing user data: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"), e);
            throw new RuntimeException("Failed to initialize user data", e);
        }
    }

    @FXML
    private void handleTypeDocumentChange() {
        String selectedType = typeDocumentCombo.getValue();
        if (selectedType != null && documentPrices.containsKey(selectedType)) {
            Double price = documentPrices.get(selectedType);
            prixField.setText(String.format("%.2f", price));
            typeDocumentCombo.setStyle(""); // Reset style on valid selection
        } else {
            prixField.clear();
            typeDocumentCombo.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
        }
    }

    private boolean validateCIN() {
        String cin = idUserField.getText().trim();
        if (cin.isEmpty()) {
            idUserField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showAlert("Erreur", "Le champ CIN ne peut pas être vide.");
            return false;
        }
        if (cin.length() != 8) {
            idUserField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showAlert("Erreur", "La CIN doit contenir exactement 8 chiffres.");
            return false;
        }
        if (!cin.matches("[01][0-9]{7}")) {
            idUserField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showAlert("Erreur", "La CIN doit commencer par 0 ou 1 et contenir 8 chiffres.");
            return false;
        }
        idUserField.setStyle("-fx-border-color: green; -fx-border-width: 1px;");
        return true;
    }

    private boolean validateNom() {
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            nomField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showAlert("Erreur", "Le champ nom ne peut pas être vide.");
            return false;
        }
        if (!nom.matches("[a-zA-Z\\s]+")) {
            nomField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showAlert("Erreur", "Le nom ne doit contenir que des lettres et des espaces.");
            return false;
        }
        nomField.setStyle("-fx-border-color: green; -fx-border-width: 1px;");
        return true;
    }

    private boolean validateAdresse() {
        String adresse = adresseField.getText().trim();
        if (adresse.isEmpty()) {
            adresseField.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showAlert("Erreur", "Le champ adresse ne peut pas être vide.");
            return false;
        }
        adresseField.setStyle("-fx-border-color: green; -fx-border-width: 1px;");
        return true;
    }

    private boolean validateTypeDocument() {
        String selectedType = typeDocumentCombo.getValue();
        if (selectedType == null || selectedType.trim().isEmpty()) {
            typeDocumentCombo.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
            showAlert("Erreur", "Veuillez sélectionner un type de document.");
            return false;
        }
        typeDocumentCombo.setStyle("-fx-border-color: green; -fx-border-width: 1px;");
        return true;
    }

    @FXML
    private void handleOpenMap() {
        try {
            if (mapController == null) {
                mapController = new Map();
                mapController.setLinkedAddressField(adresseField);
            }
            mapController.openMap();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening map: " + e.getMessage(), e);
            showAlert("Erreur", "Problème lors de l'ouverture de la carte: " + e.getMessage());
        }
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        // Validate all fields
        if (!validateCIN() || !validateNom() || !validateAdresse() || !validateTypeDocument()) {
            return;
        }

        if (currentCitoyen == null || !SessionManager.getInstance().isCitoyen()) {
            showAlert("Erreur", "Utilisateur non connecté ou type d'utilisateur incorrect.");
            return;
        }

        try {
            Demande demande = new Demande();
            demande.setId_user(currentCitoyen.getId());
            demande.setCin(idUserField.getText());
            demande.setNom(nomField.getText());
            demande.setAdresse(adresseField.getText());
            demande.setType(typeDocumentCombo.getValue());

            totalAmount = Float.parseFloat(prixField.getText().replace(",", "."));
            demande.setPrice(totalAmount);


            paymentSessionId = stripePaymentService.createCheckoutSession(totalAmount);
            if (paymentSessionId == null || paymentSessionId.isEmpty()) {
                showAlert("Erreur", "Impossible de créer la session de paiement.");
                return;
            }

            WebView webView = new WebView();
            webView.getEngine().load(paymentSessionId);

            VBox webViewContainer = new VBox(webView);
            Scene scene = new Scene(webViewContainer, 800, 600);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Paiement Stripe");

            webView.getEngine().locationProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.contains("success")) {
                    Platform.runLater(() -> {
                        stage.close();
                        paymentCompleted = true;
                        confirmPayment(demande);
                    });
                } else if (newVal != null && newVal.contains("cancel")) {
                    Platform.runLater(() -> {
                        stage.close();
                        showAlert("Annulé", "Le paiement a été annulé.");
                    });
                }
            });

            stage.show();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during payment submission: " + e.getMessage(), e);

            showAlert("Erreur", "Une erreur s'est produite lors du paiement: " + e.getMessage());
        }
    }

    private void confirmPayment(Demande demande) {
        try {
            // Add the demand
            serviceDemande.ajouter(demande);
            LOGGER.info("Demande recorded with ID: " + demande.getId_demande());

            // Create and add a corresponding document
            Document document = new Document();
            document.setId_demande(demande.getId_demande());
            document.setType_docs(demande.getType());
            document.setStatut_doc("En traitement");
            document.setDate_emission_doc(new java.sql.Date(System.currentTimeMillis()));

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.add(java.util.Calendar.MONTH, 3);
            document.setDate_expiration_doc(new java.sql.Date(calendar.getTimeInMillis()));

            document.setArchive(false);
            document.setNb_req(1);
            document.setId_citoyen(currentCitoyen.getId());

            serviceDocument.add(document);
            LOGGER.info("Document created with status 'En traitement' for demande ID: " + demande.getId_demande());

            resetForm();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during payment confirmation: " + e.getMessage(), e);
            showAlert("Erreur", "Erreur lors de l'enregistrement dans la base de données: " + e.getMessage());
        }
    }

    @FXML
    private void resetForm() {
        if (!userFieldsPrefilled) {
            idUserField.clear();
            nomField.clear();
        }
        typeDocumentCombo.setValue(null);
        adresseField.clear();
        prixField.clear();
        paymentCompleted = false;

        // Reset field styles
        idUserField.setStyle("");
        nomField.setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 0.85;");
        adresseField.setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 0.85;");
        typeDocumentCombo.setStyle("");
    }

    private void showAlert(String title, String message) {
        Alert.AlertType type = title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showProfile(ActionEvent event) {
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

    public void logout(ActionEvent event) { SessionManager.getInstance().clearSession();


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
