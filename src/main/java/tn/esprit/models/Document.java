package tn.esprit.models;

import java.util.Date;

public class Document {
    private int id_doc;
    private String type_docs;
    private String statut_doc; // En traitement, Validé, Rejeté
    private Date date_emission_doc;
    private Date date_expiration_doc;
    private boolean archive;
    private int nb_req;
    private int id_demande; // Pour lier le document à une demande
    private int id_citoyen; // Pour lier le document au citoyen

    // Constructeurs
    public Document() {
    }

    public Document(int id_doc, String type_docs, String statut_doc, Date date_emission_doc,
                    Date date_expiration_doc, boolean archive, int nb_req, int id_demande, int id_citoyen) {
        this.id_doc = id_doc;
        this.type_docs = type_docs;
        this.statut_doc = statut_doc;
        this.date_emission_doc = date_emission_doc;
        this.date_expiration_doc = date_expiration_doc;
        this.archive = archive;
        this.nb_req = nb_req;
        this.id_demande = id_demande;
        this.id_citoyen = id_citoyen;
    }

    public Document(String type_docs, String statut_doc, Date date_emission_doc,
                    Date date_expiration_doc, boolean archive, int nb_req, int id_demande, int id_citoyen) {
        this.type_docs = type_docs;
        this.statut_doc = statut_doc;
        this.date_emission_doc = date_emission_doc;
        this.date_expiration_doc = date_expiration_doc;
        this.archive = archive;
        this.nb_req = nb_req;
        this.id_demande = id_demande;
        this.id_citoyen = id_citoyen;
    }

    // Getters et Setters
    public int getId_doc() {
        return id_doc;
    }

    public void setId_doc(int id_doc) {
        this.id_doc = id_doc;
    }

    public String getType_docs() {
        return type_docs;
    }

    public void setType_docs(String type_docs) {
        this.type_docs = type_docs;
    }

    public String getStatut_doc() {
        return statut_doc;
    }

    public void setStatut_doc(String statut_doc) {
        this.statut_doc = statut_doc;
    }

    public Date getDate_emission_doc() {
        return date_emission_doc;
    }

    public void setDate_emission_doc(Date date_emission_doc) {
        this.date_emission_doc = date_emission_doc;
    }

    public Date getDate_expiration_doc() {
        return date_expiration_doc;
    }

    public void setDate_expiration_doc(Date date_expiration_doc) {
        this.date_expiration_doc = date_expiration_doc;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public int getNb_req() {
        return nb_req;
    }

    public void setNb_req(int nb_req) {
        this.nb_req = nb_req;
    }

    public int getId_demande() {
        return id_demande;
    }

    public void setId_demande(int id_demande) {
        this.id_demande = id_demande;
    }

    public int getId_citoyen() {
        return id_citoyen;
    }

    public void setId_citoyen(int id_citoyen) {
        this.id_citoyen = id_citoyen;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id_doc=" + id_doc +
                ", type_docs='" + type_docs + '\'' +
                ", statut_doc='" + statut_doc + '\'' +
                ", date_emission_doc=" + date_emission_doc +
                ", date_expiration_doc=" + date_expiration_doc +
                ", archive=" + archive +
                ", nb_req=" + nb_req +
                ", id_demande=" + id_demande +
                ", id_citoyen=" + id_citoyen +
                '}';
    }
}