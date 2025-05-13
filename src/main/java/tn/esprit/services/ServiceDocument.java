package tn.esprit.services;

import tn.esprit.models.Citoyen;
import tn.esprit.models.Document;
import tn.esprit.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
    
    /**
     * Gets all documents from the database
     * @return List of all documents
     * @throws SQLException if there's a database error
     */
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
                
                // Handle timestamp fields
                java.sql.Timestamp emissionTs = rs.getTimestamp("date_emission_doc");
                if (emissionTs != null) {
                    document.setDate_emission_doc(new java.util.Date(emissionTs.getTime()));
                }
                
                java.sql.Timestamp expirationTs = rs.getTimestamp("date_expiration_doc");
                if (expirationTs != null) {
                    document.setDate_expiration_doc(new java.util.Date(expirationTs.getTime()));
                }
                
                document.setArchive(rs.getBoolean("archive"));
                document.setNb_req(rs.getInt("nb_req"));
                
                // Handle nullable foreign keys
                int id_demande = rs.getInt("id_demande");
                if (!rs.wasNull()) {
                    document.setId_demande(id_demande);
                }
                
                int id_citoyen = rs.getInt("id_citoyen");
                if (!rs.wasNull()) {
                    document.setId_citoyen(id_citoyen);
                }
                
                documents.add(document);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all documents: " + e.getMessage());
            throw e;
        }
        
        // Mock data for testing if connection is null (should never happen in production)
        if (connection == null) {
            documents.add(new Document(1, "Certificat", "Validé", new java.util.Date(), null, false, 1, 1, 1));
            documents.add(new Document(2, "Permis", "En traitement", new java.util.Date(), null, true, 2, 2, 2));
        }
        return documents;
    }

    /**
     * Fetch documents by demand type
     * @param demandeType the type of document to filter by
     * @return List of documents matching the specified type
     * @throws SQLException if there's a database error
     */
    public List<Document> getDocumentsByDemandeType(String demandeType) throws SQLException {
        List<Document> documents = new ArrayList<>();
        
        // Use direct filtering on type_docs - simpler and more reliable approach
        String query = "SELECT * FROM document WHERE type_docs = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, demandeType);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Document document = new Document();
                document.setId_doc(rs.getInt("id_doc"));
                document.setType_docs(rs.getString("type_docs"));
                document.setStatut_doc(rs.getString("statut_doc"));
                
                // Handle timestamp fields
                java.sql.Timestamp emissionTs = rs.getTimestamp("date_emission_doc");
                if (emissionTs != null) {
                    document.setDate_emission_doc(new java.util.Date(emissionTs.getTime()));
                }
                
                java.sql.Timestamp expirationTs = rs.getTimestamp("date_expiration_doc");
                if (expirationTs != null) {
                    document.setDate_expiration_doc(new java.util.Date(expirationTs.getTime()));
                }
                
                document.setArchive(rs.getBoolean("archive"));
                document.setNb_req(rs.getInt("nb_req"));
                
                // Handle nullable foreign keys
                int id_demande = rs.getInt("id_demande");
                if (!rs.wasNull()) {
                    document.setId_demande(id_demande);
                }
                
                int id_citoyen = rs.getInt("id_citoyen");
                if (!rs.wasNull()) {
                    document.setId_citoyen(id_citoyen);
                }
                
                documents.add(document);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching documents by type: " + e.getMessage());
            throw e; // Rethrow to allow proper handling at the caller level
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
        // Use direct counting with type_docs - simpler and more reliable approach
        String query = "SELECT COUNT(*) FROM document WHERE type_docs = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, demandeType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting documents by type: " + e.getMessage());
            throw e; // Rethrow to allow proper handling at the caller level
        }
        
        // Mock data for testing
        if (connection == null) {
            return demandeType.equals("Plans urbanisme") ? 1 : demandeType.equals("Registre état civil") ? 1 : 0;
        }
        return 0;
    }

    /**
     * Get citizen information by document ID
     * @param documentId the ID of the document
     * @return Citoyen object associated with the document, or null if not found
     * @throws SQLException if there's a database error
     */
    public Citoyen getCitoyenByDocumentId(int documentId) throws SQLException {
        try {
            // First get the id_citoyen from the document
            String docQuery = "SELECT id_citoyen FROM document WHERE id_doc = ?";
            Integer id_citoyen = null;
            
            try (PreparedStatement docStmt = connection.prepareStatement(docQuery)) {
                docStmt.setInt(1, documentId);
                ResultSet docRs = docStmt.executeQuery();
                if (docRs.next()) {
                    id_citoyen = docRs.getInt("id_citoyen");
                    if (docRs.wasNull()) {
                        id_citoyen = null;
                    }
                } else {
                    return null; // Document not found
                }
            }
            
            // If no citoyen associated with document
            if (id_citoyen == null) {
                return null;
            }
            
            // Then get the citizen information from utilisateur table
            String citizenQuery = "SELECT id_user, nom_user, prenom_user, cin FROM utilisateur WHERE id_user = ?";
            try (PreparedStatement citizenStmt = connection.prepareStatement(citizenQuery)) {
                citizenStmt.setInt(1, id_citoyen);
                ResultSet rs = citizenStmt.executeQuery();
                if (rs.next()) {
                    Citoyen citoyen = new Citoyen();
                    citoyen.setId(rs.getInt("id_user"));
                    citoyen.setCin(rs.getString("cin"));
                    citoyen.setNom(rs.getString("nom_user"));
                    citoyen.setPrenom(rs.getString("prenom_user"));
                    return citoyen;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting citizen by document ID: " + e.getMessage());
            throw e; // Rethrow to allow proper handling at the caller level
        }
        
        // Mock data for testing
        if (connection == null && documentId == 1) {
            return new Citoyen(1, "12345678", "Doe", "John");
        } else if (connection == null && documentId == 2) {
            return new Citoyen(2, "87654321", "Smith", "Jane");
        }
        return null;
    }

    /**
     * Update archive status of a document
     * @param documentId the ID of the document to update
     * @param archive the new archive status
     * @throws SQLException if there's a database error
     */
    public void updateArchiveStatus(int documentId, boolean archive) throws SQLException {
        try {
            // Use transaction for data integrity
            connection.setAutoCommit(false);
            
            String query = "UPDATE document SET archive = ? WHERE id_doc = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setBoolean(1, archive);
                stmt.setInt(2, documentId);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    throw new SQLException("Document with ID " + documentId + " not found.");
                }
                
                // Commit the transaction
                connection.commit();
            }
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during transaction rollback: " + rollbackEx.getMessage());
                throw rollbackEx;
            }
            System.err.println("Error updating archive status: " + e.getMessage());
            throw e;
        } finally {
            // Reset auto-commit
            try {
                connection.setAutoCommit(true);
            } catch (SQLException resetEx) {
                System.err.println("Error resetting auto-commit: " + resetEx.getMessage());
            }
        }
    }

    // Get document type by document ID
    public String getDemandeTypeByDocumentId(int documentId) throws SQLException {
        // Get document type directly - simpler and more reliable approach
        String query = "SELECT type_docs FROM document WHERE id_doc = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, documentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("type_docs");
            }
        } catch (SQLException e) {
            System.err.println("Error getting document type: " + e.getMessage());
            throw e; // Rethrow to allow proper handling at the caller level
        }
        
        // Mock data for testing
        if (connection == null && documentId == 1) {
            return "Plans urbanisme";
        } else if (connection == null && documentId == 2) {
            return "Registre état civil";
        }
        return null;
    }

    /**
     * Update status of a document
     * @param documentId the ID of the document to update
     * @param newStatus the new status
     * @throws SQLException if there's a database error
     */
    public void updateStatus(int documentId, String newStatus) throws SQLException {
        try {
            // Use transaction for data integrity
            connection.setAutoCommit(false);
            
            String query = "UPDATE document SET statut_doc = ? WHERE id_doc = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, documentId);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    throw new SQLException("Document with ID " + documentId + " not found.");
                }
                
                // Commit the transaction
                connection.commit();
            }
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during transaction rollback: " + rollbackEx.getMessage());
                throw rollbackEx;
            }
            System.err.println("Error updating document status: " + e.getMessage());
            throw e;
        } finally {
            // Reset auto-commit
            try {
                connection.setAutoCommit(true);
            } catch (SQLException resetEx) {
                System.err.println("Error resetting auto-commit: " + resetEx.getMessage());
            }
        }
    }

    /**
     * Delete a document by ID
     * @param idDoc the ID of the document to delete
     * @throws SQLException if there's a database error
     */
    public void delete(int idDoc) throws SQLException {
        if (idDoc <= 0) {
            throw new IllegalArgumentException("Invalid document ID: " + idDoc);
        }
        
        try {
            // Use transaction for data integrity
            connection.setAutoCommit(false);
            
            String query = "DELETE FROM document WHERE id_doc = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, idDoc);
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected == 0) {
                    throw new SQLException("Document with ID " + idDoc + " not found or could not be deleted.");
                }
                
                // Commit the transaction
                connection.commit();
                System.out.println("Document with ID " + idDoc + " successfully deleted.");
            }
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during transaction rollback: " + rollbackEx.getMessage());
                throw rollbackEx;
            }
            System.err.println("Error deleting document: " + e.getMessage());
            throw e;
        } finally {
            // Reset auto-commit
            try {
                connection.setAutoCommit(true);
            } catch (SQLException resetEx) {
                System.err.println("Error resetting auto-commit: " + resetEx.getMessage());
            }
        }
    }
    
    /**
     * Add a new document
     * @param document the document to add
     * @throws SQLException if there's a database error
     */
    public void add(Document document) throws SQLException {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }
        
        try {
            // Use transaction for data integrity
            connection.setAutoCommit(false);
            
            String query = "INSERT INTO document (type_docs, statut_doc, date_emission_doc, date_expiration_doc, " +
                          "archive, nb_req, id_citoyen, id_demande) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, document.getType_docs());
                stmt.setString(2, document.getStatut_doc());
                
                // Handle timestamp fields
                if (document.getDate_emission_doc() != null) {
                    stmt.setTimestamp(3, new java.sql.Timestamp(document.getDate_emission_doc().getTime()));
                } else {
                    stmt.setNull(3, java.sql.Types.TIMESTAMP);
                }
                
                if (document.getDate_expiration_doc() != null) {
                    stmt.setTimestamp(4, new java.sql.Timestamp(document.getDate_expiration_doc().getTime()));
                } else {
                    stmt.setNull(4, java.sql.Types.TIMESTAMP);
                }
                
                stmt.setBoolean(5, document.isArchive());
                stmt.setInt(6, document.getNb_req());
                
                // Handle nullable foreign keys
                if (document.getId_citoyen() > 0) {
                    stmt.setInt(7, document.getId_citoyen());
                } else {
                    stmt.setNull(7, java.sql.Types.INTEGER);
                }
                
                if (document.getId_demande() > 0) {
                    stmt.setInt(8, document.getId_demande());
                } else {
                    stmt.setNull(8, java.sql.Types.INTEGER);
                }
                
                stmt.executeUpdate();
                
                // Commit the transaction
                connection.commit();
                System.out.println("Document successfully added.");
            }
        } catch (SQLException e) {
            // Rollback transaction on error
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during transaction rollback: " + rollbackEx.getMessage());
                throw rollbackEx;
            }
            System.err.println("Error adding document: " + e.getMessage());
            throw e;
        } finally {
            // Reset auto-commit
            try {
                connection.setAutoCommit(true);
            } catch (SQLException resetEx) {
                System.err.println("Error resetting auto-commit: " + resetEx.getMessage());
            }
        }
    }
}
