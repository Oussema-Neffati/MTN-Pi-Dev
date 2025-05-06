package tn.esprit.models;

public class Citoyen extends Utilisateur {
    private String cin;
    private String adresse;
    private String telephone;

    public Citoyen() {
        super();
        this.setRole(Role.CITOYEN);
    }

    public Citoyen(String nom, String prenom, String email, String motDePasse, boolean actif,
                   String cin, String adresse, String telephone) {
        super(nom, prenom, email, motDePasse, Role.CITOYEN, actif);
        this.cin = cin;
        this.adresse = adresse;
        this.telephone = telephone;
    }

    public Citoyen(int id, String nom, String prenom, String email, String motDePasse, boolean actif,
                   String cin, String adresse, String telephone) {
        super(id, nom, prenom, email, motDePasse, Role.CITOYEN, actif);
        this.cin = cin;
        this.adresse = adresse;
        this.telephone = telephone;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return "Citoyen{" +
                super.toString() +
                ", cin='" + cin + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                '}';
    }
}
