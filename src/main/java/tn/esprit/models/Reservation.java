package tn.esprit.models;

import java.util.Date;

public class Reservation {
    private int idRes;
    private Date dateReservation;
    private String heureDebut;
    private String heureFin;
    private String status;
    private int nombreParticipants;
    private String motif;
    private int idUtilisateur;
    private int idRessource;

    public Reservation() {
    }

    public Reservation(Date dateReservation, String heureDebut, String heureFin, String status, int nombreParticipants, String motif, int idUtilisateur, int idRessource) {
        this.dateReservation = dateReservation;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.status = status;
        this.nombreParticipants = nombreParticipants;
        this.motif = motif;
        this.idUtilisateur = idUtilisateur;
        this.idRessource = idRessource;
    }

    public Reservation(int idRes, Date dateReservation, String heureDebut, String heureFin, String status, int nombreParticipants, String motif, int idUtilisateur, int idRessource) {
        this.idRes = idRes;
        this.dateReservation = dateReservation;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.status = status;
        this.nombreParticipants = nombreParticipants;
        this.motif = motif;
        this.idUtilisateur = idUtilisateur;
        this.idRessource = idRessource;
    }

    public int getIdRes() {
        return idRes;
    }

    public void setIdRes(int idRes) {
        this.idRes = idRes;
    }

    public Date getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(Date dateReservation) {
        this.dateReservation = dateReservation;
    }

    public String getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNombreParticipants() {
        return nombreParticipants;
    }

    public void setNombreParticipants(int nombreParticipants) {
        this.nombreParticipants = nombreParticipants;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdRessource() {
        return idRessource;
    }

    public void setIdRessource(int idRessource) {
        this.idRessource = idRessource;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idRes=" + idRes +
                ", dateReservation=" + dateReservation +
                ", heureDebut='" + heureDebut + '\'' +
                ", heureFin='" + heureFin + '\'' +
                ", status='" + status + '\'' +
                ", nombreParticipants=" + nombreParticipants +
                ", motif='" + motif + '\'' +
                ", idUtilisateur=" + idUtilisateur +
                ", idRessource=" + idRessource +
                '}';
    }
}