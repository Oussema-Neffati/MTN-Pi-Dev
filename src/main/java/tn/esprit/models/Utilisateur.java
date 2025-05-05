package tn.esprit.models;

import java.time.LocalDateTime;
import java.util.List;

public class Utilisateur {

    private int idUser;
    private String nomUser;
    private String prenomUser;
    private String email;
    private String motDePasse;
    private Role role;
    private boolean actif;
    private LocalDateTime dateCreation;
    private List<Demande> demandes;  // Ajout de la relation un-à-plusieurs

    public enum Role {
        CITOYEN, EMPLOYE, ADMIN
    }

    // Constructeur par défaut
    public Utilisateur() {
    }

    // Constructeur avec paramètres
    public Utilisateur(int idUser, String nomUser, String prenomUser, String email,
                       String motDePasse, Role role, boolean actif, LocalDateTime dateCreation, List<Demande> demandes) {
        this.idUser = idUser;
        this.nomUser = nomUser;
        this.prenomUser = prenomUser;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.actif = actif;
        this.dateCreation = dateCreation;
        this.demandes = demandes;  // Initialisation des demandes
    }

    // Getters et Setters

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNomUser() {
        return nomUser;
    }

    public void setNomUser(String nomUser) {
        this.nomUser = nomUser;
    }

    public String getPrenomUser() {
        return prenomUser;
    }

    public void setPrenomUser(String prenomUser) {
        this.prenomUser = prenomUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public List<Demande> getDemandes() {
        return demandes;
    }

    public void setDemandes(List<Demande> demandes) {
        this.demandes = demandes;
    }

    // toString
    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUser=" + idUser +
                ", nomUser='" + nomUser + '\'' +
                ", prenomUser='" + prenomUser + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", actif=" + actif +
                ", dateCreation=" + dateCreation +
                ", demandes=" + demandes + // Affichage des demandes liées
                '}';
    }
}
