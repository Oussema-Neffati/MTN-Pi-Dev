package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import tn.esprit.models.Role;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ServiceUtilisateur;

public class SignupViewController {
    @FXML
    private Button CreerCompte;

    @FXML
    private TextField EmailTFs;

    @FXML
    private TextField NomTFs;

    @FXML
    private TextField PrenomTFs;

    @FXML
    private ComboBox<Role> RoleCB;

    @FXML
    private Hyperlink gologinpage;

    @FXML
    private TextField passwordTFs;

    @FXML
    void CreerCompte(ActionEvent event) {
        ServiceUtilisateur serviceUtilisateur = new ServiceUtilisateur();
        Utilisateur user = new Utilisateur();
        user.setNom(NomTFs.getText());
        user.setPrenom(PrenomTFs.getText());
        user.setEmail(EmailTFs.getText());
        user.setMotDePasse(passwordTFs.getText());


        serviceUtilisateur.add(user);

    }

    @FXML
    void gologinpage(ActionEvent event) {

    }
}
