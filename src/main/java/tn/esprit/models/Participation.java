package tn.esprit.models;

import java.time.LocalDateTime;

public class Participation {

    private int idParticipation;
    private int idEvenement;
    private int id_user;
    private String statut;
    private int nombreticket;
    private LocalDateTime datepay; // Updated to LocalDateTime for timestamp

    public Participation() {
    }

    public Participation(int idParticipation, int idEvenement, int id_user, String statut, int nombreticket, LocalDateTime datepay) {
        this.idParticipation = idParticipation;
        this.idEvenement = idEvenement;
        this.id_user = id_user;
        this.statut = statut;
        this.nombreticket = nombreticket;
        this.datepay = datepay;
    }

    public Participation(int idEvenement, int id_user, String statut, int nombreticket, LocalDateTime datepay) {
        this.idEvenement = idEvenement;
        this.id_user = id_user;
        this.statut = statut;
        this.nombreticket = nombreticket;
        this.datepay = datepay;
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

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getNombreticket() {
        return nombreticket;
    }

    public void setNombreticket(int nombreticket) {
        this.nombreticket = nombreticket;
    }

    public LocalDateTime getDatepay() {
        return datepay;
    }

    public void setDatepay(LocalDateTime datepay) {
        this.datepay = datepay;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "idParticipation=" + idParticipation +
                ", idEvenement=" + idEvenement +
                ", id_user=" + id_user +
                ", statut='" + statut + '\'' +
                ", nombreticket=" + nombreticket +
                ", datepay=" + datepay +
                '}';
    }
}