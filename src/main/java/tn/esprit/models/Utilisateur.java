package tn.esprit.models;

public class Utilisateur {
    private int id;
    private String nom, prenom, email, motDePasse;
    private String Role;
    private boolean actif;


    public Utilisateur(int id,String nom, String prenom, String email, String motDePasse, String role, boolean actif) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        Role = role;
        this.actif = actif;
    }

    public Utilisateur(String nom, String prenom, String email, String motDePasse, String role, boolean actif) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        Role = role;
        this.actif = actif;
    }

    public Utilisateur() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
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

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", id=" + id +
                ", motDePasse='" + motDePasse + '\'' +
                ", email='" + email + '\'' +
                ", Role='" + Role + '\'' +
                ", actif=" + actif +
                '}';
    }
}