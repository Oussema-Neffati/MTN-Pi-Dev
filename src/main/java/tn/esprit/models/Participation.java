package tn.esprit.models;
public class Participation {

    private int idParticipation;
    private int idEvenement;
    private int id_user;
    private String statut;


    public Participation() {
    }


    public Participation(int idParticipation, int idEvenement, int id_user, String statut) {
        this.idParticipation = idParticipation;
        this.idEvenement = idEvenement;
        this.id_user = id_user;
        this.statut = statut;
    }


    public Participation(int idEvenement, int id_user, String statut) {
        this.idEvenement = idEvenement;
        this.id_user = id_user;
        this.statut = statut;
    }


    public int getIdParticipation() {
        return idParticipation;
    }


    public void setIdParticipation(int idParticipation) {
        this.idParticipation = idParticipation;
    }


    public int getIdEvenement() {
        return idEvenement;
    }


    public void setIdEvenement(int idEvenement) {
        this.idEvenement = idEvenement;
    }


    public int getId_user() {
        return id_user;
    }
    public void setId_user(int id_user) {}

    public String getStatut() {
        return statut;
    }


    public void setStatut(String statut) {
        this.statut = statut;
    }


    @Override
    public String toString() {
        return "Participation{" +
                "idParticipation=" + idParticipation +
                ", idEvenement=" + idEvenement +
                ", id_user=" + id_user +
                ", statut='" + statut + '\'' +
                '}';
    }
}
