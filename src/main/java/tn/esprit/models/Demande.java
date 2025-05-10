package tn.esprit.models;

public class Demande {
    private int id_demande;
    private int id_user;
    private String nom;     // nom associé à id_user
    private String adresse;
    private String type;
    private float price;

    // Constructeur par défaut
    public Demande() {
    }

    // Constructeur avec paramètres
    public Demande(int id_user, String nom, String adresse, String type, float price) {
        this.id_user = id_user;
        this.nom = nom;
        this.adresse = adresse;
        this.type = type;
        this.price = price;
    }

    // Constructeur avec id_demande
    public Demande(int id_demande, int id_user, String nom, String adresse, String type, float price) {
        this.id_demande = id_demande;
        this.id_user = id_user;
        this.nom = nom;
        this.adresse = adresse;
        this.type = type;
        this.price = price;
    }

    // Getters et Setters
    public int getId_demande() {
        return id_demande;
    }

    public void setId_demande(int id_demande) {
        this.id_demande = id_demande;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Demande{" +
                "id_demande=" + id_demande +
                ", id_user=" + id_user +
                ", nom='" + nom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", type='" + type + '\'' +
                ", price=" + price +
                '}';
    }
}