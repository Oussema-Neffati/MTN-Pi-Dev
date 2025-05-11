package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.esprit.models.Role;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDashboardCardsController {
    @FXML
    private FlowPane cardsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private Label lblTotalUsers;

    @FXML
    private Label lblCitoyens;

    @FXML
    private Label lblEmployes;

    @FXML
    private Label lblInactifs;

    @FXML
    private Button btnAddUser;

    @FXML
    private ScrollPane scrollPane;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
    private List<Utilisateur> allUsers;

    @FXML
    void initialize() {
        // Vérifier que l'utilisateur est un administrateur
        if (!SessionManager.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Vous n'avez pas les droits d'accès à cette interface.");
            retourLogin(new ActionEvent());
            return;
        }

        // Configurer la barre de recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });

        // Configurer le cardsContainer pour être responsive
        cardsContainer.prefWrapLengthProperty().bind(scrollPane.widthProperty().subtract(40));

        // Charger les données initiales
        refreshCards();
    }

    @FXML
    void refreshCards() {
        // Charger tous les utilisateurs
        allUsers = serviceUtilisateur.getAll();

        // Mettre à jour les statistiques
        updateStatistics();

        // Afficher les cards
        displayUsers(allUsers);
    }

    private void updateStatistics() {
        int total = allUsers.size();
        int citoyens = (int) allUsers.stream()
                .filter(u -> u.getRole() == Role.CITOYEN)
                .count();
        int employes = (int) allUsers.stream()
                .filter(u -> u.getRole() == Role.EMPLOYE)
                .count();
        int inactifs = (int) allUsers.stream()
                .filter(u -> !u.isActif())
                .count();

        lblTotalUsers.setText(String.valueOf(total));
        lblCitoyens.setText(String.valueOf(citoyens));
        lblEmployes.setText(String.valueOf(employes));
        lblInactifs.setText(String.valueOf(inactifs));
    }

    private void displayUsers(List<Utilisateur> users) {
        cardsContainer.getChildren().clear();

        for (Utilisateur user : users) {
            if (user.getRole() != Role.ADMIN) { // Ne pas afficher les admins
                AnchorPane card = createUserCard(user);
                cardsContainer.getChildren().add(card);
            }
        }
    }

    private AnchorPane createUserCard(Utilisateur user) {
        // Augmenter la largeur des cards pour une meilleure lisibilité
        AnchorPane card = new AnchorPane();
        card.setPrefSize(350.0, 200.0);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; " +
                "-fx-border-color: #E0E0E0; -fx-border-width: 1; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");

        // Avatar circle
        Circle avatarCircle = new Circle(30);
        avatarCircle.setLayoutX(45);
        avatarCircle.setLayoutY(50);
        avatarCircle.setFill(getAvatarColor(user.getRole()));

        // Avatar text (initiales)
        Label avatarText = new Label(getInitials(user));
        avatarText.setLayoutX(30);
        avatarText.setLayoutY(35);
        avatarText.setTextFill(Color.WHITE);
        avatarText.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Nom et prénom
        Label nameLabel = new Label(user.getPrenom() + " " + user.getNom());
        nameLabel.setLayoutX(85);
        nameLabel.setLayoutY(30);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLabel.setTextFill(Color.web("#333333"));

        // Email
        Label emailLabel = new Label(user.getEmail());
        emailLabel.setLayoutX(85);
        emailLabel.setLayoutY(48);
        emailLabel.setFont(Font.font("System", 12));
        emailLabel.setTextFill(Color.web("#817e7e"));

        // Badge rôle
        Label roleBadge = new Label(user.getRole().name());
        roleBadge.setLayoutX(20);
        roleBadge.setLayoutY(90);
        roleBadge.setPrefSize(80, 23);
        roleBadge.setAlignment(Pos.CENTER);
        roleBadge.setStyle(getRoleStyle(user.getRole()));
        roleBadge.setFont(Font.font("System", FontWeight.BOLD, 10));

        // Badge statut actif
        Label statusBadge = new Label(user.isActif() ? "ACTIF" : "INACTIF");
        statusBadge.setLayoutX(110);
        statusBadge.setLayoutY(90);
        statusBadge.setPrefSize(65, 23);
        statusBadge.setAlignment(Pos.CENTER);
        statusBadge.setStyle(getStatusStyle(user.isActif()));
        statusBadge.setFont(Font.font("System", FontWeight.BOLD, 10));

        // Informations supplémentaires selon le rôle
        Label infoLabel = new Label();
        infoLabel.setLayoutX(85);
        infoLabel.setLayoutY(65);
        infoLabel.setFont(Font.font("System", 11));
        infoLabel.setTextFill(Color.web("#757575"));

        if (user.getRole() == Role.CITOYEN) {
            tn.esprit.models.Citoyen citoyen = serviceUtilisateur.getCitoyenById(user.getId());
            if (citoyen != null && citoyen.getCin() != null) {
                infoLabel.setText("CIN: " + citoyen.getCin());
            }
        } else if (user.getRole() == Role.EMPLOYE) {
            tn.esprit.models.Employe employe = serviceUtilisateur.getEmployeById(user.getId());
            if (employe != null && employe.getPoste() != null) {
                infoLabel.setText("Poste: " + employe.getPoste());
            }
        }

        // Conteneur des boutons - Ajuster la largeur pour meilleure lisibilité
        HBox buttonBox = new HBox(8);
        buttonBox.setLayoutX(15);
        buttonBox.setLayoutY(150);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // Bouton toggle actif/inactif
        Button toggleButton = new Button(user.isActif() ? "Désactiver" : "Activer");
        toggleButton.setPrefSize(100, 32);
        toggleButton.setStyle(user.isActif()
                ? "-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;"
                : "-fx-background-color: #51CF66; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
        toggleButton.setFont(Font.font("System", FontWeight.BOLD, 11));
        toggleButton.setOnAction(event -> toggleUserStatus(user));

        // Bouton supprimer
        Button deleteButton = new Button("Supprimer");
        deleteButton.setPrefSize(95, 32);
        deleteButton.setStyle("-fx-background-color: #DB4495; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
        deleteButton.setFont(Font.font("System", FontWeight.BOLD, 11));
        deleteButton.setOnAction(event -> deleteUser(user));

        // Ajouter seulement les boutons activer/désactiver et supprimer
        buttonBox.getChildren().addAll(toggleButton, deleteButton);

        // Ajouter tous les éléments à la card
        card.getChildren().addAll(
                avatarCircle, avatarText, nameLabel, emailLabel,
                roleBadge, statusBadge, infoLabel, buttonBox
        );

        return card;
    }

    private String getInitials(Utilisateur user) {
        String initials = "";
        if (user.getPrenom() != null && !user.getPrenom().isEmpty()) {
            initials += user.getPrenom().charAt(0);
        }
        if (user.getNom() != null && !user.getNom().isEmpty()) {
            initials += user.getNom().charAt(0);
        }
        return initials.toUpperCase();
    }

    private Color getAvatarColor(Role role) {
        switch (role) {
            case CITOYEN:
                return Color.web("#4169E1");
            case EMPLOYE:
                return Color.web("#51CF66");
            default:
                return Color.web("#757575");
        }
    }

    private String getRoleStyle(Role role) {
        String baseStyle = "-fx-background-radius: 8; -fx-alignment: center;";
        switch (role) {
            case CITOYEN:
                return baseStyle + " -fx-background-color: #E7F3FF; -fx-text-fill: #4169E1;";
            case EMPLOYE:
                return baseStyle + " -fx-background-color: #E8F8F5; -fx-text-fill: #51CF66;";
            default:
                return baseStyle + " -fx-background-color: #F8F9FA; -fx-text-fill: #757575;";
        }
    }

    private String getStatusStyle(boolean isActive) {
        if (isActive) {
            return "-fx-background-color: #E8F8F5; -fx-text-fill: #51CF66; -fx-background-radius: 8; -fx-alignment: center;";
        } else {
            return "-fx-background-color: #FFE8E6; -fx-text-fill: #FF6B6B; -fx-background-radius: 8; -fx-alignment: center;";
        }
    }

    private void toggleUserStatus(Utilisateur user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText(user.isActif()
                ? "Voulez-vous désactiver cet utilisateur ?"
                : "Voulez-vous activer cet utilisateur ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            user.setActif(!user.isActif());
            serviceUtilisateur.update(user);
            refreshCards();
        }
    }

    private void deleteUser(Utilisateur user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet utilisateur ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceUtilisateur.delete(user);
            refreshCards();
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "Utilisateur supprimé avec succès.");
        }
    }

    private void filterUsers(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            displayUsers(allUsers);
        } else {
            String search = searchText.toLowerCase().trim();
            List<Utilisateur> filteredUsers = allUsers.stream()
                    .filter(user ->
                            user.getNom().toLowerCase().contains(search) ||
                                    user.getPrenom().toLowerCase().contains(search) ||
                                    user.getEmail().toLowerCase().contains(search) ||
                                    user.getRole().name().toLowerCase().contains(search))
                    .collect(Collectors.toList());
            displayUsers(filteredUsers);
        }
    }

    @FXML
    void addNewUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/SignupView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Ajouter un nouvel utilisateur");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger l'interface d'ajout d'utilisateur.");
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
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle("Connexion");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
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