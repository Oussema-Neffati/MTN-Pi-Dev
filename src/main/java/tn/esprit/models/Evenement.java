package tn.esprit.models;

public class Evenement {
    private int id;
    private String nom;
    private String lieu;
    private String date;
    private String organisateur;
    private float prix;

    public Evenement() {}

    public Evenement(int id, String nom, String lieu, String date, String organisateur, float prix) {
        this.id = id;
        this.nom = nom;
        this.lieu = lieu;
        this.date = date;
        this.organisateur = organisateur;
        this.prix = prix;
    }

    public Evenement(String nom, String lieu, String date, String organisateur, float prix) {
        this.nom = nom;
        this.lieu = lieu;
        this.date = date;
        this.organisateur = organisateur;
        this.prix = prix;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getOrganisateur() { return organisateur; }
    public void setOrganisateur(String organisateur) { this.organisateur = organisateur; }
    public float getPrix() { return prix; }
    public void setPrix(float prix) { this.prix = prix; }
    @Override
    public String toString() {
        return "Evenement{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", lieu='" + lieu + '\'' +
                ", date='" + date + '\'' +
                ", organisateur='" + organisateur + '\'' +
                ", prix=" + prix +
                '}';
    }
}