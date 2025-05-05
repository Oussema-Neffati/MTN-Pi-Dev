package tn.esprit.models;

public class DocumentRequest {

    private int id_d_doc;  // id d_doc sous forme d'int
    private String type_d_doc;
    private String description_d_doc;
    private Status statut_d_doc;
    private String date_d_doc;
    private String date_traitement_d_doc;
    private int id_doc;  // id_doc sous forme d'int

    public DocumentRequest(int id_d_doc, String type_d_doc, String description_d_doc,
                           Status statut_d_doc, String date_d_doc, int id_doc) {
        this.id_d_doc = id_d_doc;
        this.type_d_doc = type_d_doc;
        this.description_d_doc = description_d_doc;
        this.statut_d_doc = statut_d_doc;
        this.date_d_doc = date_d_doc;
        this.id_doc = id_doc;
    }

    public DocumentRequest() {
    }

    // Getters et Setters
    public int getId_d_doc() {
        return id_d_doc;
    }

    public void setId_d_doc(int id_d_doc) {
        this.id_d_doc = id_d_doc;
    }

    public String getType_d_doc() {
        return type_d_doc;
    }

    public void setType_d_doc(String type_d_doc) {
        this.type_d_doc = type_d_doc;
    }

    public String getDescription_d_doc() {
        return description_d_doc;
    }

    public void setDescription_d_doc(String description_d_doc) {
        this.description_d_doc = description_d_doc;
    }

    public Status getStatut_d_doc() {
        return statut_d_doc;
    }

    public void setStatut_d_doc(Status statut_d_doc) {
        this.statut_d_doc = statut_d_doc;
    }

    public String getDate_d_doc() {
        return date_d_doc;
    }

    public void setDate_d_doc(String date_d_doc) {
        this.date_d_doc = date_d_doc;
    }

    public int getId_doc() {
        return id_doc;
    }

    public void setId_doc(int id_doc) {
        this.id_doc = id_doc;
    }

    public String getDate_traitement_d_doc() {
        return date_traitement_d_doc;
    }

    public void setDate_traitement_d_doc(String date_traitement_d_doc) {
        this.date_traitement_d_doc = date_traitement_d_doc;
    }
}
