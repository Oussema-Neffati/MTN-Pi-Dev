package tn.esprit.Service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.interfaces.IServiceDoc;
import tn.esprit.models.Demande;
import tn.esprit.models.Utilisateur;
import tn.esprit.utils.DataBase;

import java.sql.*;

public class ServiceDemande implements IServiceDoc<Demande> {
    private final Connection cnx;

    public ServiceDemande() {
        cnx = DataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new RuntimeException("Erreur de connexion à la base de données.");
        }
    }

    @Override
    public void ajouter(Demande demande) throws SQLException {
        String sql = "INSERT INTO demande (id_user, nom, adresse, type, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, demande.getId_user());
            preparedStatement.setString(2, demande.getNom());
            preparedStatement.setString(3, demande.getAdresse());
            preparedStatement.setString(4, demande.getType());
            preparedStatement.setFloat(5, demande.getPrice());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        demande.setId_demande(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Demande ajoutée avec succès. ID: " + demande.getId_demande());
            } else {
                System.out.println("Aucune demande ajoutée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de la demande : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void modifier(Demande demande) throws SQLException {
        String sql = "UPDATE demande SET id_user = ?, nom = ?, adresse = ?, type = ?, price = ? WHERE id_demande = ?";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(sql)) {
            preparedStatement.setInt(1, demande.getId_user());
            preparedStatement.setString(2, demande.getNom());
            preparedStatement.setString(3, demande.getAdresse());
            preparedStatement.setString(4, demande.getType());
            preparedStatement.setFloat(5, demande.getPrice());
            preparedStatement.setInt(6, demande.getId_demande());

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Demande modifiée avec succès.");
            } else {
                System.out.println("Aucune demande modifiée. Vérifiez l'ID: " + demande.getId_demande());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de la demande : " + e.getMessage());
            throw e;
        }
    }

    public void supprimer(int id_demande) throws SQLException {
        String sql = "DELETE FROM demande WHERE id_demande = ?";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(sql)) {
            preparedStatement.setInt(1, id_demande);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Demande supprimée avec succès.");
            } else {
                System.out.println("Aucune demande supprimée. Vérifiez l'ID: " + id_demande);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la demande : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public ObservableList<Demande> afficher() throws SQLException {
        ObservableList<Demande> demandes = FXCollections.observableArrayList();
        String sql = "SELECT * FROM demande";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Demande d = new Demande();
                d.setId_demande(rs.getInt("id_demande"));
                d.setId_user(rs.getInt("id_user"));
                d.setNom(rs.getString("nom"));
                d.setAdresse(rs.getString("adresse"));
                d.setType(rs.getString("type"));
                d.setPrice(rs.getFloat("price"));
                demandes.add(d);
            }
        }
        return demandes;
    }


    public Demande getDemandeById(int id_demande) throws SQLException {
        String sql = "SELECT * FROM demande WHERE id_demande = ?";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(sql)) {
            preparedStatement.setInt(1, id_demande);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Demande d = new Demande();
                    d.setId_demande(rs.getInt("id_demande"));
                    d.setId_user(rs.getInt("id_user"));
                    d.setNom(rs.getString("nom"));
                    d.setAdresse(rs.getString("adresse"));
                    d.setType(rs.getString("type"));
                    d.setPrice(rs.getFloat("price"));
                    return d;
                }
            }
        }
        return null;
    }


    public ObservableList<Demande> getDemandesByUser(int idUser) throws SQLException {
        ObservableList<Demande> demandes = FXCollections.observableArrayList();
        String sql = "SELECT * FROM demande WHERE id_user = ?";

        try (PreparedStatement preparedStatement = cnx.prepareStatement(sql)) {
            preparedStatement.setInt(1, idUser);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Demande d = new Demande();
                    d.setId_demande(rs.getInt("id_demande"));
                    d.setId_user(rs.getInt("id_user"));
                    d.setNom(rs.getString("nom"));
                    d.setAdresse(rs.getString("adresse"));
                    d.setType(rs.getString("type"));
                    d.setPrice(rs.getFloat("price"));
                    demandes.add(d);
                }
            }
        }
        return demandes;
    }


    public void setUserNameInDemande(Demande demande) throws SQLException {
        Utilisateur utilisateur = getUtilisateurById(demande.getId_user());
        if (utilisateur != null) {
            demande.setNom(utilisateur.getNomUser() + " " + utilisateur.getPrenomUser());
        }
    }

    private Utilisateur getUtilisateurById(int idUser) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE id = ?";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(sql)) {
            preparedStatement.setInt(1, idUser);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setIdUser(resultSet.getInt("id"));
                    utilisateur.setNomUser(resultSet.getString("nom"));
                    utilisateur.setPrenomUser(resultSet.getString("prenom"));
                    utilisateur.setEmail(resultSet.getString("email"));
                    utilisateur.setMotDePasse(resultSet.getString("mot_de_passe"));
                    utilisateur.setRole(Utilisateur.Role.valueOf(resultSet.getString("role")));
                    utilisateur.setActif(resultSet.getBoolean("actif"));
                    utilisateur.setDateCreation(resultSet.getTimestamp("date_creation").toLocalDateTime());
                    return utilisateur;
                }
            }
        }
        return null;
    }
}