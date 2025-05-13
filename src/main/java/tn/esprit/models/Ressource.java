package tn.esprit.models;

/**
 * Entity class representing a resource in the system
 */
public class Ressource {

    private int id;
    private String nom;
    private String categorie;
    private int capacite;
    private double tarifHoraire;
    private String horaireOuverture;
    private String horaireFermeture;
    private String description;
    private boolean disponible;

    /**
     * Default constructor
     */
    public Ressource() {
    }

    /**
     * Parameterized constructor
     */
    public Ressource(int id, String nom, String categorie, int capacite, double tarifHoraire,
                     String horaireOuverture, String horaireFermeture, String description, boolean disponible) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.capacite = capacite;
        this.tarifHoraire = tarifHoraire;
        this.horaireOuverture = horaireOuverture;
        this.horaireFermeture = horaireFermeture;
        this.description = description;
        this.disponible = disponible;
    }

    // Getters and Setters

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

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public double getTarifHoraire() {
        return tarifHoraire;
    }

    public void setTarifHoraire(double tarifHoraire) {
        this.tarifHoraire = tarifHoraire;
    }

    public String getHoraireOuverture() {
        return horaireOuverture;
    }

    public void setHoraireOuverture(String horaireOuverture) {
        this.horaireOuverture = horaireOuverture;
    }

    public String getHoraireFermeture() {
        return horaireFermeture;
    }

    public void setHoraireFermeture(String horaireFermeture) {
        this.horaireFermeture = horaireFermeture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return "Ressource{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", categorie='" + categorie + '\'' +
                ", capacite=" + capacite +
                ", tarifHoraire=" + tarifHoraire +
                ", horaireOuverture='" + horaireOuverture + '\'' +
                ", horaireFermeture='" + horaireFermeture + '\'' +
                ", description='" + description + '\'' +
                ", disponible=" + disponible +
                '}';
    }
}