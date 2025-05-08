package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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

    private tn.esprit.controllers.Map mapController;

    // Map pour stocker les types de documents et leurs prix associés
    private HashMap<String, Double> documentPrices;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le contrôleur de carte
        mapController = new tn.esprit.controllers.Map();
        mapController.setLinkedAddressField(adresseField);

        // Définir un événement sur le bouton de carte
        if (mapButton != null) {
            mapButton.setOnAction(event -> handleOpenMap());
        }

        // Configurer le contrôle de saisie pour le champ CIN
        setupCINValidation();

        // Initialiser les types de documents et leurs prix
        initializeDocumentTypes();

        // Configurer le champ de prix comme non éditable
        prixField.setEditable(false);
    }

    /**
     * Initialise les types de documents dans le ComboBox et leurs prix associés
     */
    private void initializeDocumentTypes() {
        // Créer la map des prix pour chaque type de document
        documentPrices = new HashMap<>();
        documentPrices.put("Plans urbanisme", 75.00);
        documentPrices.put("Registre état civil", 25.00);
        documentPrices.put("Autorisation construction", 150.00);
        documentPrices.put("Règlements municipalité", 50.00);
        documentPrices.put("Légalisation d'un papier", 15.00);

        // Créer la liste des types de documents pour le ComboBox
        ObservableList<String> documentTypes = FXCollections.observableArrayList(documentPrices.keySet());

        // Configurer le ComboBox
        typeDocumentCombo.setItems(documentTypes);
    }

    /**
     * Gère le changement de sélection dans le ComboBox des types de documents
     */
    @FXML
    public void handleTypeDocumentChange() {
        String selectedType = typeDocumentCombo.getValue();

        if (selectedType != null && documentPrices.containsKey(selectedType)) {
            // Récupérer le prix associé au type sélectionné
            Double price = documentPrices.get(selectedType);

            // Afficher le prix dans le champ de prix
            prixField.setText(String.format("%.2f", price));
        } else {
            // Effacer le champ de prix si aucun type n'est sélectionné
            prixField.clear();
        }
    }

    /**
     * Configure la validation en temps réel du champ CIN
     */
    private void setupCINValidation() {
        // Filtrer les caractères lors de la saisie
        idUserField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String character = event.getCharacter();

            // Vérifier si le caractère n'est pas un chiffre
            if (!character.matches("[0-9]")) {
                event.consume(); // Bloquer la saisie
                return;
            }

            // Vérifier que le premier caractère est soit 0 soit 1
            if (idUserField.getText().isEmpty() && !character.matches("[01]")) {
                event.consume(); // Bloquer la saisie
                return;
            }

            // Limiter à 8 chiffres
            if (idUserField.getText().length() >= 8) {
                event.consume(); // Bloquer la saisie
            }
        });

        // Ajouter un écouteur pour la validation complète lors de la perte de focus
        idUserField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // Quand le champ perd le focus
                validateCIN();
            }
        });
    }

    /**
     * Valide le format de la CIN
     * @return true si le format est valide, false sinon
     */
    private boolean validateCIN() {
        String cin = idUserField.getText();

        // Vérifier si la CIN est vide
        if (cin.isEmpty()) {
            return false;
        }

        // Vérifier si la CIN a exactement 8 chiffres
        if (cin.length() != 8) {
            showAlert("Erreur de saisie", "La CIN doit contenir exactement 8 chiffres.");
            return false;
        }

        // Vérifier si la CIN commence par 0 ou 1
        char firstChar = cin.charAt(0);
        if (firstChar != '0' && firstChar != '1') {
            showAlert("Erreur de saisie", "La CIN doit commencer par 0 ou 1.");
            return false;
        }

        return true;
    }

    /**
     * Gère le chargement des informations utilisateur à partir de la CIN
     */
    @FXML
    public void handleFetchUserInfo() {
        String cin = idUserField.getText();

        // Vérifier si la CIN est valide
        if (!validateCIN()) {
            return;
        }

        // TODO: Ici, ajouter le code pour récupérer les informations de l'utilisateur
        // depuis la base de données en utilisant la CIN fournie

        // Exemple temporaire (à remplacer par une vraie récupération de données)
        if ("12345678".equals(cin)) {
            nomField.setText("Oussema Neffati");
            adresseField.setText("Tunis, Tunisie");
        } else {
            showAlert("Information", "Aucun utilisateur trouvé avec cette CIN.");
        }
    }

    /**
     * Ouvre la fenêtre de carte pour sélectionner une adresse
     */
    @FXML
    public void handleOpenMap() {
        try {
            System.out.println("Opening map...");
            // Vérifier que le champ d'adresse est correctement lié
            if (adresseField == null) {
                System.err.println("adresseField is null");
                showAlert("Erreur", "Champ d'adresse non initialisé");
                return;
            }

            // S'assurer que le contrôleur de carte est correctement configuré
            if (mapController == null) {
                mapController = new tn.esprit.controllers.Map();
                mapController.setLinkedAddressField(adresseField);
            }

            mapController.openMap();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Problème lors de l'ouverture de la carte: " + e.getMessage());
        }
    }

    /**
     * Gère la soumission du formulaire de demande
     */
    @FXML
    public void handleSubmit() {
        // Vérifier si la CIN est valide
        if (!validateCIN()) {
            return;
        }

        // Vérifier si tous les champs obligatoires sont remplis
        if (nomField.getText().isEmpty() ||
                typeDocumentCombo.getValue() == null ||
                adresseField.getText().isEmpty() ||
                prixField.getText().isEmpty()) {

            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        // Vérification du prix déjà effectuée par le système car le champ n'est pas modifiable
        // et est automatiquement défini en fonction du type de document

        // TODO: Ajouter le code pour enregistrer la demande dans la base de données

        // Afficher un message de confirmation
        showAlert("Succès", "Votre demande a été soumise avec succès!");

        // Réinitialiser le formulaire
        resetForm();
    }

    /**
     * Réinitialise tous les champs du formulaire
     */
    @FXML
    public void resetForm() {
        idUserField.clear();
        nomField.clear();
        typeDocumentCombo.setValue(null);
        adresseField.clear();
        prixField.clear();
    }

    /**
     * Affiche une boîte de dialogue d'alerte
     */
    private void showAlert(String title, String message) {
        Alert.AlertType type = title.equals("Succès") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}