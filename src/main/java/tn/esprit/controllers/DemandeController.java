package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.services.ServiceDemande;
import tn.esprit.models.Citoyen;
import tn.esprit.models.Demande;
import tn.esprit.models.Utilisateur;
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

    private tn.esprit.controllers.Map mapController;
    private ServiceDemande serviceDemande;

    // Map pour stocker les types de documents et leurs prix associés
    private HashMap<String, Double> documentPrices;

    // Indique si les champs utilisateur ont été préremplis
    private boolean userFieldsPrefilled = false;
    private Utilisateur currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser le service de demande
        serviceDemande = new ServiceDemande();

        // Initialiser le contrôleur de carte
        mapController = new tn.esprit.controllers.Map();
        mapController.setLinkedAddressField(adresseField);

        // Définir un événement sur le bouton de carte
        if (mapButton != null) {
            mapButton.setOnAction(event -> handleOpenMap());
        }

        // Configurer le contrôle de saisie pour le champ CIN
        setupCINValidation();

        // Configurer le contrôle de saisie pour le champ nom
        setupNomValidation();

        // Rendre le champ adresse non éditable
        adresseField.setEditable(false);

        // Initialiser les types de documents et leurs prix
        initializeDocumentTypes();

        // Configurer le champ de prix comme non éditable
        prixField.setEditable(false);

        // Style commun pour les champs non éditables
        String readOnlyStyle = "-fx-background-color: #f0f0f0; -fx-opacity: 0.85;";
        prixField.setStyle(readOnlyStyle);

        // Préremplir les données de l'utilisateur
        initializeUserData();
    }

    public void initializeUserData() {
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Vérifier si un utilisateur est connecté et s'il est citoyen
        if (currentUser != null && SessionManager.getInstance().isCitoyen()) {
            try {
                if (currentUser instanceof Citoyen) {
                    Citoyen citoyen = (Citoyen) currentUser;

                    // Préremplir le champ CIN
                    String cin = citoyen.getCin() != null && !citoyen.getCin().isEmpty()
                            ? citoyen.getCin()
                            : String.valueOf(currentUser.getId());
                    idUserField.setText(cin);
                    idUserField.setEditable(false);
                    idUserField.setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 0.85;");

                    // Préremplir le champ nom avec prénom et nom
                    String nomComplet = (citoyen.getPrenom() != null ? citoyen.getPrenom() : "") + " "
                            + (citoyen.getNom() != null ? citoyen.getNom() : "");
                    nomComplet = nomComplet.trim();
                    if (nomComplet.isEmpty()) {
                        nomComplet = "Nom non disponible";
                    }
                    nomField.setText(nomComplet);
                    nomField.setEditable(false);
                    nomField.setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 0.85;");

                    // Mettre à jour le label en haut avec le nom complet
                    if (currentUserLabel != null) {
                        currentUserLabel.setText(nomComplet);
                    }

                    // Préremplir l'adresse si disponible
                    if (citoyen.getAdresse() != null && !citoyen.getAdresse().isEmpty()) {
                        adresseField.setText(citoyen.getAdresse());
                    }

                    userFieldsPrefilled = true;
                } else {
                    showAlert("Erreur", "L'utilisateur connecté n'est pas un citoyen.");
                }
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger les données utilisateur : " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Erreur", "Aucun utilisateur citoyen connecté.");
        }
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
            if (!idUserField.isEditable()) {
                event.consume(); // Bloquer toute saisie si non éditable
                return;
            }
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
            if (!newValue && idUserField.isEditable()) { // Valider uniquement si éditable
                validateCIN();
            }
        });
    }

    /**
     * Configure la validation en temps réel du champ nom
     */
    private void setupNomValidation() {
        // Filtrer les caractères lors de la saisie
        nomField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (!nomField.isEditable()) {
                event.consume(); // Bloquer toute saisie si non éditable
                return;
            }
            String character = event.getCharacter();

            // Vérifier si le caractère est une lettre ou un espace
            if (!character.matches("[a-zA-Z\\s]")) {
                event.consume(); // Bloquer la saisie de chiffres et caractères spéciaux
                return;
            }
        });

        // Ajouter un écouteur pour la validation complète lors de la perte de focus
        nomField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && nomField.isEditable()) { // Valider uniquement si éditable
                validateNom();
            }
        });
    }

    /**
     * Valide le format du nom
     * @return true si le format est valide, false sinon
     */
    private boolean validateNom() {
        String nom = nomField.getText().trim();

        // Vérifier si le nom est vide
        if (nom.isEmpty()) {
            showAlert("Erreur de saisie", "Le champ nom ne peut pas être vide.");
            return false;
        }

        // Vérifier si le nom contient uniquement des lettres et des espaces
        if (!nom.matches("[a-zA-Z\\s]+")) {
            showAlert("Erreur de saisie", "Le nom ne doit contenir que des lettres et des espaces.");
            return false;
        }

        return true;
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
     * Ouvre la fenêtre de carte pour sélectionner une adresse
     */
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
        if (!validateCIN()) {
            return;
        }
        if (!validateNom()) {
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

        try {
            // Vérifier que l'utilisateur est bien connecté
            if (currentUser == null) {
                showAlert("Erreur", "Aucun utilisateur connecté. Veuillez vous connecter.");
                return;
            }

            // Créer une nouvelle demande
            Demande demande = new Demande();
            // Utiliser l'ID de l'utilisateur connecté
            demande.setId_user(currentUser.getId());
            demande.setCin(idUserField.getText());
            demande.setNom(nomField.getText());
            demande.setAdresse(adresseField.getText());
            demande.setType(typeDocumentCombo.getValue());

            // Convertir le prix de String à float
            try {
                demande.setPrice(Float.parseFloat(prixField.getText().replace(",", ".")));
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Format de prix invalide");
                return;
            }

            // Ajouter la demande à la base de données
            serviceDemande.ajouter(demande);

            // Afficher un message de succès avec l'ID de la demande créée
            showAlert("Succès", "Votre demande a été soumise avec succès! ID de la demande: " + demande.getId_demande());

            // Réinitialiser le formulaire après soumission
            resetForm();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement dans la base de données : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur inattendue s'est produite : " + e.getMessage());
        }
    }

    /**
     * Réinitialise tous les champs du formulaire
     */
    @FXML
    public void resetForm() {
        if (!userFieldsPrefilled) {
            idUserField.clear();
            nomField.clear();
        }

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