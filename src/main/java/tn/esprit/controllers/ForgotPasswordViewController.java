package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;
import tn.esprit.utils.ValidationUtils;

import java.io.IOException;
import java.util.Random;

public class ForgotPasswordViewController {

    @FXML
    private TextField emailField;

    @FXML
    private Button resetButton;

    @FXML
    private Hyperlink backToLogin;

    private ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();

    @FXML
    void initialize() {
        // Configuration initiale si nécessaire
    }

    @FXML
    void handleResetPassword(ActionEvent event) {
        String email = emailField.getText();

        // Validation de l'email
        if (ValidationUtils.isNullOrEmpty(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer votre adresse email.");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer une adresse email valide.");
            return;
        }

        // Vérifier si l'utilisateur existe
        Utilisateur user = serviceUtilisateur.findByEmail(email);
        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun compte n'est associé à cette adresse email.");
            return;
        }

        // Générer un nouveau mot de passe temporaire
        String newPassword = generateTemporaryPassword();

        // Mettre à jour le mot de passe dans la base de données
        user.setMotDePasse(newPassword);
        serviceUtilisateur.update(user);

        // Simuler l'envoi d'un email (dans une vraie application, vous enverriez un vrai email)
        showAlert(Alert.AlertType.INFORMATION, "Succès",
                "Un nouveau mot de passe temporaire a été généré.\n\n" +
                        "Nouveau mot de passe : " + newPassword + "\n\n" +
                        "Veuillez vous connecter avec ce mot de passe et le changer dans vos paramètres.\n\n" +
                        "(Dans une vraie application, ce mot de passe serait envoyé par email)");

        // Rediriger vers la page de connexion
        handleBackToLogin(event);
    }

    @FXML
    void handleBackToLogin(ActionEvent event) {
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

    private String generateTemporaryPassword() {
        // Générer un mot de passe temporaire
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        // Assurer qu'il y a au moins une majuscule, une minuscule et un chiffre
        password.append(chars.charAt(random.nextInt(26))); // Majuscule
        password.append(chars.charAt(26 + random.nextInt(26))); // Minuscule
        password.append(chars.charAt(52 + random.nextInt(10))); // Chiffre

        // Générer les 5 caractères restants
        for (int i = 0; i < 5; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        // Mélanger les caractères
        return shuffleString(password.toString());
    }

    private String shuffleString(String string) {
        char[] characters = string.toCharArray();
        Random random = new Random();

        for (int i = 0; i < characters.length; i++) {
            int randomIndex = random.nextInt(characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }

        return new String(characters);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}