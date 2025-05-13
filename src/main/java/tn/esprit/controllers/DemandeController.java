package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.esprit.models.Demande;
import tn.esprit.models.Document;
import tn.esprit.models.Utilisateur;
import tn.esprit.models.Citoyen;
import tn.esprit.services.ServiceDemande;
import tn.esprit.services.ServiceDocument;
import tn.esprit.services.StripePayment;
import tn.esprit.utils.SessionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

public class DemandeController implements Initializable {

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

    @FXML
    private Button downloadTicketButton;

    @FXML
    private Label statusLabel;

    private Map mapController;
    private ServiceDemande serviceDemande;
    private StripePayment stripePaymentService;
    private Utilisateur currentUser;
    private Citoyen currentCitoyen;
    private HashMap<String, Double> documentPrices;
    private boolean userFieldsPrefilled = false;
    private String paymentSessionId;
    private float totalAmount;
    private boolean paymentCompleted = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Initialize services with exception handling
            initializeServices();
            // Initialize UI components
            initializeUIComponents();
            // Initialize map controller
            initializeMapController();
            // Set up map button
            setupMapButton();
            // Set up field validations and styles
            setupFieldValidations();
            // Initialize document types
            initializeDocumentTypes();
            // Initialize user data
            initializeUserData();
        } catch (Exception e) {
            System.err.println("Unexpected error during initialization of DemandeController: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur inattendue lors de l'initialisation: " + e.getMessage());
        }
    }

    private void initializeServices() {
        try {
            serviceDemande = new ServiceDemande();
        } catch (Exception e) {
            System.err.println("Failed to initialize ServiceDemande: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'initialisation du service de demande: " + e.getMessage());
        }
        try {
            stripePaymentService = new StripePayment();
        } catch (Exception e) {
            System.err.println("Failed to initialize StripePayment: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'initialisation du service de paiement: " + e.getMessage());
        }
    }

    private void initializeUIComponents() {
        try {
            downloadTicketButton.setVisible(false);
            statusLabel.setVisible(false);
        } catch (Exception e) {
            System.err.println("Failed to initialize UI components: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeMapController() {
        try {
            mapController = new Map();
            mapController.setLinkedAddressField(adresseField);
        } catch (Exception e) {
            System.err.println("Failed to initialize Map controller: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'initialisation de la carte: " + e.getMessage());
        }
    }

    private void setupMapButton() {
        try {
            if (mapButton != null) {
                mapButton.setOnAction(event -> handleOpenMap());
            }
        } catch (Exception e) {
            System.err.println("Failed to set up map button: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupFieldValidations() {
        try {
            setupCINValidation();
            setupNomValidation();
            adresseField.setEditable(false);
            prixField.setEditable(false);
            String readOnlyStyle = "-fx-background-color: #f0f0f0; -fx-opacity: 0.85;";
            prixField.setStyle(readOnlyStyle);
        } catch (Exception e) {
            System.err.println("Failed to set up field validations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void initializeUserData() {
        if (currentCitoyen != null && SessionManager.getInstance().isCitoyen()) {
            try {
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
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger les données utilisateur : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Erreur", "Aucun utilisateur citoyen connecté.");
        }
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
    }

    @FXML
    public void handleTypeDocumentChange() {
        String selectedType = typeDocumentCombo.getValue();
        if (selectedType != null && documentPrices.containsKey(selectedType)) {
            Double price = documentPrices.get(selectedType);
            prixField.setText(String.format("%.2f", price));
        } else {
            prixField.clear();
        }
    }

    private void setupCINValidation() {
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

        idUserField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && idUserField.isEditable()) {
                validateCIN();
            }
        });
    }

    private void setupNomValidation() {
        nomField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!nomField.isEditable()) {
                event.consume();
                return;
            }
            String character = event.getCharacter();
            if (!character.matches("[a-zA-Z\\s]")) {
                event.consume();
                return;
            }
        });

        nomField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && nomField.isEditable()) {
                validateNom();
            }
        });
    }

    private boolean validateNom() {
        String nom = nomField.getText().trim();
        if (nom.isEmpty()) {
            showAlert("Erreur", "Le champ nom ne peut pas être vide.");
            return false;
        }
        if (!nom.matches("[a-zA-Z\\s]+")) {
            showAlert("Erreur", "Le nom ne doit contenir que des lettres et des espaces.");
            return false;
        }
        return true;
    }

    private boolean validateCIN() {
        String cin = idUserField.getText();
        if (cin.isEmpty()) {
            return false;
        }
        if (cin.length() != 8) {
            showAlert("Erreur", "La CIN doit contenir exactement 8 chiffres.");
            return false;
        }
        char firstChar = cin.charAt(0);
        if (firstChar != '0' && firstChar != '1') {
            showAlert("Erreur", "La CIN doit commencer par 0 ou 1.");
            return false;
        }
        return true;
    }

    @FXML
    public void handleOpenMap() {
        try {
            System.out.println("Opening map...");
            if (adresseField == null) {
                System.err.println("adresseField is null");
                showAlert("Erreur", "Champ d'adresse non initialisé");
                return;
            }
            if (mapController == null) {
                mapController = new Map();
                mapController.setLinkedAddressField(adresseField);
            }
            mapController.openMap();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème lors de l'ouverture de la carte: " + e.getMessage());
        }
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        if (!validateCIN() || !validateNom()) {
            return;
        }

        if (nomField.getText().isEmpty() || typeDocumentCombo.getValue() == null ||
                adresseField.getText().isEmpty() || prixField.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (currentUser == null) {
            showAlert("Erreur", "Aucun utilisateur connecté. Veuillez vous connecter.");
            return;
        }

        try {
            Demande demande = new Demande();
            demande.setId_user(currentUser.getId());
            demande.setCin(idUserField.getText());
            demande.setNom(nomField.getText());
            demande.setAdresse(adresseField.getText());
            demande.setType(typeDocumentCombo.getValue());

            try {
                totalAmount = Float.parseFloat(prixField.getText().replace(",", "."));
                demande.setPrice(totalAmount);
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Format de prix invalide");
                return;
            }

            paymentSessionId = StripePayment.createCheckoutSession(totalAmount);
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

            webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    String location = newValue.toString();
                    if (location.contains("success")) {
                        Platform.runLater(() -> {
                            stage.close();
                            paymentCompleted = true;
                            confirmPayment(demande);
                        });
                    }
                }
            });

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Une erreur s'est produite lors du paiement.", e.getMessage());
        }
    }

    private void confirmPayment(Demande demande) {
        try {
            // Add the demand to the demande table
            serviceDemande.ajouter(demande);
            downloadTicketButton.setVisible(true);
            showAlert("Succès", "Demande enregistrée avec succès! ID: " + demande.getId_demande());

            // Create and add a corresponding document entry with status "en traitement"
            ServiceDocument serviceDocument = new ServiceDocument();
            Document document = new Document();
            document.setId_demande(demande.getId_demande()); // Link to the demand
            document.setType_docs(demande.getType()); // Copy the type from the demand
            document.setStatut_doc("en traitement"); // Default status
            document.setDate_emission_doc(new java.sql.Date(System.currentTimeMillis())); // Current date
            document.setDate_expiration_doc(null); // No expiration date initially
            document.setArchive(false); // Not archived by default
            document.setNb_req(1); // Default to 1 request

            // Set id_citoyen based on currentUser (assuming currentUser is a Citoyen)
            if (currentUser != null && SessionManager.getInstance().isCitoyen()) {
                document.setId_citoyen(currentUser.getId());
            } else {
                document.setId_citoyen(0); // Set to null in DB if not a citizen
            }

            serviceDocument.add(document); // Use the add method from ServiceDocument
            System.out.println("Document created with status 'en traitement' for demande ID: " + demande.getId_demande());

            resetForm(); // Reset the form after successful submission
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement dans la base de données : " + e.getMessage());
        }
    }
    @FXML
    public void resetForm() {
        if (!userFieldsPrefilled) {
            idUserField.clear();
            nomField.clear();
        }
        typeDocumentCombo.setValue(null);
        adresseField.clear();
        prixField.clear();
        downloadTicketButton.setVisible(false);
        paymentCompleted = false;
    }

    private void showAlert(String title, String message) {
        Alert.AlertType type = title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}