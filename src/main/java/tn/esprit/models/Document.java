package tn.esprit.models;

import java.util.Objects;

public class Document {
        private int id_doc;
        private String type_doc;
        private String statut_doc;
        private String date_emission_doc;
        private String date_expiration_doc;
        private boolean archive;
        private int nb_req;

    public Document() {
    }

    public Document(int id_doc, int nb_req, boolean archive,
                    String date_expiration_doc, String date_emission_doc,
                    String statut_doc, String type_doc) {
        this.id_doc = id_doc;
        this.nb_req = nb_req;
        this.archive = archive;
        this.date_expiration_doc = date_expiration_doc;
        this.date_emission_doc = date_emission_doc;
        this.statut_doc = statut_doc;
        this.type_doc = type_doc;
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

    public String getDate_expiration_doc() {
        return date_expiration_doc;
    }

    public void setDate_expiration_doc(String date_expiration_doc) {
        this.date_expiration_doc = date_expiration_doc;
    }

    public String getDate_emission_doc() {
        return date_emission_doc;
    }

    public void setDate_emission_doc(String date_emission_doc) {
        this.date_emission_doc = date_emission_doc;
    }

    public String getStatut_doc() {
        return statut_doc;
    }

    public void setStatut_doc(String statut_doc) {
        this.statut_doc = statut_doc;
    }

    public String getType_doc() {
        return type_doc;
    }

    public void setType_doc(String type_doc) {
        this.type_doc = type_doc;
    }

    public int getId_doc() {
        return id_doc;
    }

    public void setId_doc(int id_doc) {
        this.id_doc = id_doc;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id_doc=" + id_doc +
                ", type_doc='" + type_doc + '\'' +
                ", statut_doc='" + statut_doc + '\'' +
                ", date_emission_doc='" + date_emission_doc + '\'' +
                ", date_expiration_doc='" + date_expiration_doc + '\'' +
                ", archive=" + archive +
                ", nb_req=" + nb_req +
                '}';
    }
}