package tn.esprit.models;

import java.time.LocalDate;

public class Employe extends Utilisateur {
    private String poste;
    private LocalDate dateEmbauche;
    private String departement;

    public Employe() {
        super();
        this.setRole(Role.EMPLOYE);
    }

    public Employe(String nom, String prenom, String email, String motDePasse, boolean actif,
                   String poste, LocalDate dateEmbauche, String departement) {
        super(nom, prenom, email, motDePasse, Role.EMPLOYE, actif);
        this.poste = poste;
        this.dateEmbauche = dateEmbauche;
        this.departement = departement;
    }

    public Employe(int id, String nom, String prenom, String email, String motDePasse, boolean actif,
                    String poste, LocalDate dateEmbauche, String departement) {
        super(id, nom, prenom, email, motDePasse, Role.EMPLOYE, actif);
        this.poste = poste;
        this.dateEmbauche = dateEmbauche;
        this.departement = departement;
    }


    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
    }

    @Override
    public String toString() {
        return "Employe{" +
                super.toString() +
                ", poste='" + poste + '\'' +
                ", dateEmbauche=" + dateEmbauche +
                ", departement='" + departement + '\'' +
                '}';
    }
}