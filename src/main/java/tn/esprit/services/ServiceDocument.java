package tn.esprit.services;

import tn.esprit.models.Citoyen;
import tn.esprit.models.Document;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceDocument {

    private Connection connection;


    public ServiceDocument() {
        // Initialize connection using MyDataBase singleton
        this.connection = MyDataBase.getInstance().getCnx();
        if (this.connection == null) {
            throw new IllegalStateException("Failed to initialize database connection");
        }
    }
    
    public List<Document> getAllDocuments() throws SQLException {
        List<Document> documents = new ArrayList<>();
        String query = "SELECT * FROM document";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Document document = new Document();
                document.setId_doc(rs.getInt("id_doc"));
                document.setType_docs(rs.getString("type_docs"));
                document.setStatut_doc(rs.getString("statut_doc"));
                document.setDate_emission_doc(rs.getDate("date_emission_doc"));
                document.setDate_expiration_doc(rs.getDate("date_expiration_doc"));
                document.setArchive(rs.getBoolean("archive"));
                document.setNb_req(rs.getInt("nb_req"));
                document.setId_demande(rs.getInt("id_demande"));
                document.setId_citoyen(rs.getInt("id_citoyen"));
                documents.add(document);
            }
        }
        // Mock data for testing
        if (connection == null) {
            documents.add(new Document(1, "Certificat", "Validé", new java.util.Date(), null, false, 1, 1, 1));
            documents.add(new Document(2, "Permis", "En traitement", new java.util.Date(), null, true, 2, 2, 2));
        }
        return documents;
    }

    // Fetch documents by demand type
    public List<Document> getDocumentsByDemandeType(String demandeType) throws SQLException {
        List<Document> documents = new ArrayList<>();
        String query = "SELECT d.* FROM document d JOIN demande de ON d.id_demande = de.id_demande WHERE de.type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, demandeType);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Document document = new Document();
                document.setId_doc(rs.getInt("id_doc"));
                document.setType_docs(rs.getString("type_docs"));
                document.setStatut_doc(rs.getString("statut_doc"));
                document.setDate_emission_doc(rs.getDate("date_emission_doc"));
                document.setDate_expiration_doc(rs.getDate("date_expiration_doc"));
                document.setArchive(rs.getBoolean("archive"));
                document.setNb_req(rs.getInt("nb_req"));
                document.setId_demande(rs.getInt("id_demande"));
                document.setId_citoyen(rs.getInt("id_citoyen"));
                documents.add(document);
            }
        }
        // Mock data for testing
        if (connection == null && demandeType.equals("Plans urbanisme")) {
            documents.add(new Document(1, "Certificat", "Validé", new java.util.Date(), null, false, 1, 1, 1));
        } else if (connection == null && demandeType.equals("Registre état civil")) {
            documents.add(new Document(2, "Permis", "En traitement", new java.util.Date(), null, true, 2, 2, 2));
        }
        return documents;
    }

    // Count documents by demand type
    public long countDocumentsByDemandeType(String demandeType) throws SQLException {
        String query = "SELECT COUNT(*) FROM document d JOIN demande de ON d.id_demande = de.id_demande WHERE de.type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, demandeType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        // Mock data for testing
        if (connection == null) {
            return demandeType.equals("Plans urbanisme") ? 1 : demandeType.equals("Registre état civil") ? 1 : 0;
        }
        return 0;
    }

    // Get citizen by document ID
    public Citoyen getCitoyenByDocumentId(int documentId) throws SQLException {
        String query = "SELECT u.* FROM utilisateur u JOIN document d ON u.id = d.id_citoyen WHERE d.id_doc = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, documentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Citoyen citoyen = new Citoyen();
                citoyen.setId(rs.getInt("id"));
                citoyen.setCin(rs.getString("cin"));
                citoyen.setNom(rs.getString("nom"));
                citoyen.setPrenom(rs.getString("prenom"));
                return citoyen;
            }
        }
        // Mock data for testing
        if (connection == null && documentId == 1) {
            return new Citoyen(1, "12345678", "Doe", "John");
        } else if (connection == null && documentId == 2) {
            return new Citoyen(2, "87654321", "Smith", "Jane");
        }
        return null;
    }

    // Update archive status
    public void updateArchiveStatus(int documentId, boolean archive) throws SQLException {
        String query = "UPDATE document SET archive = ? WHERE id_doc = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBoolean(1, archive);
            stmt.setInt(2, documentId);
            stmt.executeUpdate();
        }
    }

    // Get demand type by document ID
    public String getDemandeTypeByDocumentId(int documentId) throws SQLException {
        String query = "SELECT de.type FROM demande de JOIN document d ON de.id_demande = d.id_demande WHERE d.id_doc = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, documentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("type");
            }
        }
        // Mock data for testing
        if (connection == null && documentId == 1) {
            return "Plans urbanisme";
        } else if (connection == null && documentId == 2) {
            return "Registre état civil";
        }
        return null;
    }

    public void updateStatus(int documentId, String newStatus) throws SQLException {
        String query = "UPDATE document SET statut_doc = ? WHERE id_doc = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, documentId);
            stmt.executeUpdate();
        }
    }

    public void delete(int idDoc) {
    }
}