package tn.esprit.models;

import java.time.LocalDate;

public class Reservation {
    private int idRes;
    private LocalDate dateReservation;
    private String heureDebut;
    private String heureFin;
    private String status;
    private int nombreParticipants;
    private String motif;
    private String cin;
    private int idUtilisateur;
    private int idRessource;

    public Reservation() {
    }

    public Reservation(LocalDate dateReservation, String heureDebut, String heureFin, 
                      String status, int nombreParticipants, String motif, String cin) {
        this.dateReservation = dateReservation;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.status = status;
        this.nombreParticipants = nombreParticipants;
        this.motif = motif;
        this.cin = cin;
    }

    public Reservation(int idRes, LocalDate dateReservation, String heureDebut, String heureFin, 
                      String status, int nombreParticipants, String motif, String cin) {
        this.idRes = idRes;
        this.dateReservation = dateReservation;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.status = status;
        this.nombreParticipants = nombreParticipants;
        this.motif = motif;
        this.cin = cin;
    }

    public int getIdRes() {
        return idRes;
    }

    public void setIdRes(int idRes) {
        this.idRes = idRes;
    }

    public LocalDate getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(LocalDate dateReservation) {
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

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
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
                ", cin='" + cin + '\'' +
                ", idUtilisateur=" + idUtilisateur +
                ", idRessource=" + idRessource +
                '}';
    }
}