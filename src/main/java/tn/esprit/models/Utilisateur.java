package tn.esprit.models;

import java.util.regex.Pattern;

public class Utilisateur {
    private int id_user;
    private String nom_user, prenom_user, email, motDePasse;
    private Role role;
    private boolean actif;


    public Utilisateur(int id,String nom, String prenom, String email, String motDePasse, Role role, boolean actif) {
        this.nom_user = nom;
        this.prenom_user = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.actif = actif;
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse, Role role, boolean actif) {
        this.nom_user = nom;
        this.prenom_user = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.actif = actif;
    }

    public Utilisateur() {

    }

    public int getId() {
        return id_user;
    }

    public void setId(int id) {
        this.id_user = id;
    }

    public String getNom() {
        return nom_user;
    }

    public void setNom(String nom) {
        this.nom_user = nom;
    }

    public String getPrenom() {
        return prenom_user;
    }

    public void setPrenom(String prenom) {
        this.prenom_user = prenom;
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

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "nom='" + nom_user + '\'' +
                ", prenom='" + prenom_user + '\'' +
                ", id=" + id_user +
                ", motDePasse='" + motDePasse + '\'' +
                ", email='" + email + '\'' +
                ", Role='" + role + '\'' +
                ", actif=" + actif +
                '}';
    }
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    // Méthode pour valider les champs obligatoires
    public boolean validateRequiredFields() {
        return nom_user != null && !nom_user.isEmpty() &&
                prenom_user != null && !prenom_user.isEmpty() &&
                email != null && !email.isEmpty() && isValidEmail(email) &&
                motDePasse != null && !motDePasse.isEmpty();
    }
}
