package tn.esprit.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.interfaces.IServiceDoc;
import tn.esprit.models.DocumentRequest;
import tn.esprit.utils.DataBase;
import tn.esprit.models.Status;

import java.sql.*;

public abstract class ServiceDocumentRequest implements IServiceDoc<DocumentRequest> {
    private Connection cnx;

    public ServiceDocumentRequest() {

        cnx = DataBase.getInstance().getCnx();
    }


    public void ajouter(DocumentRequest documentRequest) throws SQLException {
        String req = "INSERT INTO `demande_document` (`type_d_doc`, `description_d_doc`, `statut_d_doc`, `date_d_doc`, `id_document`) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = cnx.prepareStatement(req)) {
            // Remplissage des paramètres de la requête
            preparedStatement.setString(1, documentRequest.getType_d_doc());
            preparedStatement.setString(2, documentRequest.getDescription_d_doc());
            preparedStatement.setString(3, documentRequest.getStatut_d_doc().name());
            preparedStatement.setString(4, documentRequest.getDate_d_doc());
            preparedStatement.setInt(5, documentRequest.getId_doc());

            // Exécution de la requête d'insertion
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du document: " + e.getMessage());
            throw e; // Rethrow the exception for further handling if needed
        }

        // Mise à jour du compteur de demandes dans la table documents
        String add_req = "UPDATE `documents` SET `nb_req` = `nb_req` + 1 WHERE `id_doc` = ?";
        try (PreparedStatement preparedStatement2 = cnx.prepareStatement(add_req)) {
            preparedStatement2.setInt(1, documentRequest.getId_doc());
            preparedStatement2.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du compteur de demandes: " + e.getMessage());
            throw e; // Propagate exception or handle as needed
        }
    }

    public void modifier(DocumentRequest documentRequest) throws SQLException {
            String req = "UPDATE `demande_document` SET `type_d_doc` = ?, `description_d_doc` = ?, `date_d_doc` = ? WHERE `id_d_doc` = ?";

            try (PreparedStatement preparedStatement = cnx.prepareStatement(req)) {
                preparedStatement.setString(1, documentRequest.getType_d_doc());
                preparedStatement.setString(2, documentRequest.getDescription_d_doc());
                preparedStatement.setString(3, documentRequest.getDate_d_doc());
                preparedStatement.setInt(4, documentRequest.getId_d_doc());
                preparedStatement.executeUpdate();
            }
        }

        // Pour citoyen
        @Override
        public ObservableList<DocumentRequest> afficher() throws SQLException {
            ObservableList<DocumentRequest> list = FXCollections.observableArrayList();
            String sql = "SELECT * FROM `demande_document`";

            try (Statement statement = cnx.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {

                while (rs.next()) {
                    DocumentRequest p = new DocumentRequest();
                    p.setId_d_doc(rs.getInt("id_d_doc"));
                    p.setType_d_doc(rs.getString("type_d_doc"));
                    p.setDescription_d_doc(rs.getString("description_d_doc"));
                    p.setStatut_d_doc(Status.valueOf(rs.getString("statut_d_doc")));
                    p.setDate_d_doc(rs.getString("date_d_doc"));
                    list.add(p);
                }
            }
            return list;
        }

        public ObservableList<DocumentRequest> afficherEnAttente() throws SQLException {
            ObservableList<DocumentRequest> list = FXCollections.observableArrayList();
            String sql = "SELECT * FROM `demande_document` WHERE `statut_d_doc` = 'EN_ATTENTE'";

            try (Statement statement = cnx.createStatement();
                 ResultSet rs = statement.executeQuery(sql)) {

                while (rs.next()) {
                    DocumentRequest p = new DocumentRequest();
                    p.setId_d_doc(rs.getInt("id_d_doc"));
                    p.setType_d_doc(rs.getString("type_d_doc"));
                    p.setDescription_d_doc(rs.getString("description_d_doc"));
                    p.setStatut_d_doc(Status.valueOf(rs.getString("statut_d_doc")));
                    p.setDate_d_doc(rs.getString("date_d_doc"));
                    p.setDate_traitement_d_doc(rs.getString("date_traitement_d_doc"));
                    list.add(p);
                }
            }
            return list;
        }

        /*--------------------------- ADMIN ---------------------------*/
        public void modifierDocsRequest(DocumentRequest dd_doc) throws SQLException {
            String req = "UPDATE `demande_document` SET `statut_d_doc` = ?, `date_traitement_d_doc` = ? WHERE `id_d_doc` = ?";

            try (PreparedStatement preparedStatement = cnx.prepareStatement(req)) {
                preparedStatement.setString(1, dd_doc.getStatut_d_doc().name());
                preparedStatement.setString(2, dd_doc.getDate_traitement_d_doc());
                preparedStatement.setInt(3, dd_doc.getId_d_doc());
                preparedStatement.executeUpdate();
            }
        }
    // el supprimer tab9a lel ADMIN
        @Override
        public void supprimer(int id) throws SQLException {
            String req = "DELETE FROM `demande_document` WHERE `id_d_doc` = ?";

            try (PreparedStatement preparedStatement = cnx.prepareStatement(req)) {
                preparedStatement.setInt(1, id);
                preparedStatement.executeUpdate();
            }
        }

    }
