package tn.esprit.controllers;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.models.Role;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.util.List;

public class AdminDashboardController {
    @FXML
    private TableView<Utilisateur> utilisateursTable;

    @FXML
    private TableColumn<Utilisateur, Integer> idColumn;

    @FXML
    private TableColumn<Utilisateur, String> nomColumn;

    @FXML
    private TableColumn<Utilisateur, String> prenomColumn;

    @FXML
    private TableColumn<Utilisateur, String> emailColumn;

    @FXML
    private TableColumn<Utilisateur, String> roleColumn;

    @FXML
    private TableColumn<Utilisateur, String> actifColumn;

    @FXML
    private TableColumn<Utilisateur, Utilisateur> actionsColumn;


    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();

    @FXML
    void initialize() {
        // Vérifier que l'utilisateur est un administrateur
        if (!SessionManager.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Vous n'avez pas les droits d'accès à cette interface.");
            retourLogin(new ActionEvent());
            return;
        }

        // Configurer les colonnes
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Pour la colonne rôle, convertir l'enum en String
        roleColumn.setCellValueFactory(cellData -> {
            Role role = cellData.getValue().getRole();
            return new SimpleStringProperty(role.name());
        });

        // Pour la colonne actif, convertir le boolean en String
        actifColumn.setCellValueFactory(cellData -> {
            boolean actif = cellData.getValue().isActif();
            return new SimpleStringProperty(actif ? "Oui" : "Non");
        });

        // Pour la colonne d'actions
        actionsColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleButton = new Button();

            @Override
            protected void updateItem(Utilisateur utilisateur, boolean empty) {
                super.updateItem(utilisateur, empty);

                if (empty || utilisateur == null) {
                    setGraphic(null);
                    return;
                }

                // Ignorer l'utilisateur admin
                if (utilisateur.getRole() == Role.ADMIN) {
                    setGraphic(null);
                    return;
                }

                // Changer le texte du bouton en fonction de l'état actif
                if (utilisateur.isActif()) {
                    toggleButton.setText("Désactiver");
                    toggleButton.setStyle("-fx-background-color: #ff6b6b;");
                } else {
                    toggleButton.setText("Activer");
                    toggleButton.setStyle("-fx-background-color: #51cf66;");
                }

                toggleButton.setOnAction(event -> {
                    // Inverser l'état actif
                    boolean newState = !utilisateur.isActif();
                    utilisateur.setActif(newState);

                    // Mettre à jour dans la base de données
                    serviceUtilisateur.update(utilisateur);

                    // Rafraîchir la table
                    refreshTable();
                });

                setGraphic(toggleButton);
            }
        });

        // Charger les données
        refreshTable();
    }

    @FXML
    void refreshTable() {
        // Vider la table
        utilisateursTable.getItems().clear();

        // Récupérer tous les utilisateurs
        List<Utilisateur> utilisateurs = serviceUtilisateur.getAll();

        // Ajouter à la table
        utilisateursTable.getItems().addAll(utilisateurs);
    }

    @FXML
    void retourLogin(ActionEvent event) {
        // Déconnecter l'utilisateur
        SessionManager.getInstance().clearSession();

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