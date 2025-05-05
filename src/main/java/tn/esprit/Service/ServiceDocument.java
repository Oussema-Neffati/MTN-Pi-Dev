package tn.esprit.Service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.interfaces.IServiceDoc;
import tn.esprit.models.Document;
import tn.esprit.models.DocumentRequest;
import tn.esprit.utils.DataBase;

import java.sql.*;

public class ServiceDocument implements IServiceDoc<Document> {
    private Connection cnx;

    public ServiceDocument() {
        cnx = DataBase.getCnx();
    }

    @Override
    public void ajouter(Document document) throws SQLException {
        String req = "INSERT INTO documents (type_doc, statut_doc, date_emission_doc, date_expiration_doc, archive, nb_req) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = cnx.prepareStatement(req)) {
            preparedStatement.setString(1, document.getType_doc());
            preparedStatement.setString(2, document.getStatut_doc());
            preparedStatement.setString(3, document.getDate_emission_doc());
            preparedStatement.setString(4, document.getDate_expiration_doc());
            preparedStatement.setBoolean(5, document.isArchive());
            preparedStatement.setInt(6, document.getNb_req());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void modifier(Document document) throws SQLException {
        String req = "UPDATE documents SET type_doc = ?, statut_doc = ?, date_emission_doc = ?, date_expiration_doc = ?, archive = ?, nb_req = ? WHERE id_doc = ?";

        try (PreparedStatement preparedStatement = cnx.prepareStatement(req)) {
            preparedStatement.setString(1, document.getType_doc());
            preparedStatement.setString(2, document.getStatut_doc());
            preparedStatement.setString(3, document.getDate_emission_doc());
            preparedStatement.setString(4, document.getDate_expiration_doc());
            preparedStatement.setBoolean(5, document.isArchive());
            preparedStatement.setInt(6, document.getNb_req());
            preparedStatement.setInt(7, document.getId_doc());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM documents WHERE id_doc = ?";

        try (PreparedStatement preparedStatement = cnx.prepareStatement(req)) {
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
        }
    }



    public void modifier(DocumentRequest documentRequest) throws SQLException {

    }

    public boolean RechercherDoc(Document d) throws SQLException {
        String sql = "SELECT * FROM documents WHERE type_doc = ?";
        ObservableList<Document> list = FXCollections.observableArrayList();

        try (PreparedStatement preparedStatement = cnx.prepareStatement(sql)) {
            preparedStatement.setString(1, d.getType_doc());
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                Document p = new Document();
                p.setId_doc(rs.getInt("id_doc"));
                list.add(p);
            }
        }

        return !list.isEmpty();
    }

    @Override
    public ObservableList<Document> afficher() throws SQLException {
        String sql = "SELECT * FROM documents";
        ObservableList<Document> list = FXCollections.observableArrayList();

        try (Statement statement = cnx.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Document p = new Document();
                p.setId_doc(rs.getInt("id_doc"));
                p.setType_doc(rs.getString("type_doc"));
                p.setStatut_doc(rs.getString("statut_doc"));
                p.setDate_emission_doc(rs.getString("date_emission_doc"));
                p.setDate_expiration_doc(rs.getString("date_expiration_doc"));
                p.setArchive(rs.getBoolean("archive"));
                p.setNb_req(rs.getInt("nb_req"));

                list.add(p);
            }
        }

        return list;
    }

    public ObservableList<Document> afficherCitoyen() throws SQLException {
        String sql = "SELECT * FROM documents WHERE archive = FALSE";
        ObservableList<Document> list = FXCollections.observableArrayList();

        try (Statement statement = cnx.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                Document p = new Document();
                p.setId_doc(rs.getInt("id_doc"));
                p.setType_doc(rs.getString("type_doc"));
                p.setStatut_doc(rs.getString("statut_doc"));
                p.setDate_emission_doc(rs.getString("date_emission_doc"));
                p.setDate_expiration_doc(rs.getString("date_expiration_doc"));
                p.setArchive(rs.getBoolean("archive"));
                p.setNb_req(rs.getInt("nb_req"));

                list.add(p);
            }
        }

        return list;
    }

    public String getNbDocs() throws SQLException {
        String sql = "SELECT COUNT(*) AS nbDocs FROM documents";

        try (Statement statement = cnx.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                return String.valueOf(rs.getInt("nbDocs"));
            } else {
                throw new SQLException("No documents found");
            }
        }
    }
}