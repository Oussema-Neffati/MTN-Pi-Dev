package tn.esprit.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.esprit.interfaces.IServiceDoc;
import tn.esprit.models.Demande;
import tn.esprit.models.Role;
import tn.esprit.models.Utilisateur;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;

public class ServiceDemande implements IServiceDoc<Demande> {
    private final Connection cnx;

    public ServiceDemande() {
        cnx = MyDataBase.getInstance().getCnx();
        if (cnx == null) {
            throw new RuntimeException("Erreur de connexion à la base de données.");
        }
    }

    /**
     * Valide une demande avant de l'enregistrer dans la base de données
     * @param demande La demande à valider
     * @throws IllegalArgumentException Si la demande est invalide
     */
    private void validateDemande(Demande demande) throws IllegalArgumentException {
        if (demande == null) {
            throw new IllegalArgumentException("La demande ne peut pas être null");
        }
        
        if (demande.getId_user() <= 0) {
            throw new IllegalArgumentException("L'ID utilisateur doit être un nombre positif");
        }
        
        if (demande.getNom() == null || demande.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }
        
        if (demande.getAdresse() == null || demande.getAdresse().trim().isEmpty()) {
            throw new IllegalArgumentException("L'adresse ne peut pas être vide");
        }
        
        if (demande.getType() == null || demande.getType().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de demande ne peut pas être vide");
        }
        
        if (demande.getPrice() < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas être négatif");
        }
    }

    @Override
    public void ajouter(Demande demande) throws SQLException {
        // Validation de la demande
        try {
            validateDemande(demande);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Validation de la demande échouée: " + e.getMessage(), e);
        }
        
        Connection localCnx = null;
        PreparedStatement preparedStatement = null;
        
        try {
            // Ouvrir une transaction locale
            localCnx = MyDataBase.getInstance().getCnx();
            localCnx.setAutoCommit(false);
            
            String sql = "INSERT INTO demande (id_user, nom, adresse, type, price) VALUES (?, ?, ?, ?, ?)";
            preparedStatement = localCnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
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
                // Commit de la transaction
                localCnx.commit();
                System.out.println("Demande ajoutée avec succès. ID: " + demande.getId_demande());
            } else {
                // Rollback en cas d'échec
                localCnx.rollback();
                throw new SQLException("Aucune demande ajoutée. Opération annulée.");
            }
        } catch (SQLException e) {
            // Rollback en cas d'exception
            if (localCnx != null) {
                try {
                    localCnx.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }
            
            String errorCode = "DB-INSERT-" + (e.getErrorCode() != 0 ? e.getErrorCode() : "UNKNOWN");
            System.err.println(errorCode + ": Erreur lors de l'ajout de la demande : " + e.getMessage());
            throw new SQLException("Erreur d'ajout de demande [" + errorCode + "]: " + e.getMessage(), e);
        } finally {
            // Fermeture des ressources
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du statement : " + e.getMessage());
                }
            }
            
            // Rétablir l'autocommit
            if (localCnx != null) {
                try {
                    localCnx.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Erreur lors du rétablissement de l'autocommit : " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void modifier(Demande demande) throws SQLException {
        // Validation de la demande
        try {
            validateDemande(demande);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Validation de la demande échouée: " + e.getMessage(), e);
        }
        
        if (demande.getId_demande() <= 0) {
            throw new SQLException("L'ID de la demande doit être un nombre positif pour la modification");
        }
        
        Connection localCnx = null;
        PreparedStatement preparedStatement = null;
        
        try {
            // Ouvrir une transaction locale
            localCnx = MyDataBase.getInstance().getCnx();
            localCnx.setAutoCommit(false);
            
            String sql = "UPDATE demande SET id_user = ?, nom = ?, adresse = ?, type = ?, price = ? WHERE id_demande = ?";
            preparedStatement = localCnx.prepareStatement(sql);
            
            preparedStatement.setInt(1, demande.getId_user());
            preparedStatement.setString(2, demande.getNom());
            preparedStatement.setString(3, demande.getAdresse());
            preparedStatement.setString(4, demande.getType());
            preparedStatement.setFloat(5, demande.getPrice());
            preparedStatement.setInt(6, demande.getId_demande());
            
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                // Commit de la transaction
                localCnx.commit();
                System.out.println("Demande modifiée avec succès. ID: " + demande.getId_demande());
            } else {
                // Rollback en cas d'échec
                localCnx.rollback();
                throw new SQLException("Aucune demande modifiée. Vérifiez l'ID: " + demande.getId_demande());
            }
        } catch (SQLException e) {
            // Rollback en cas d'exception
            if (localCnx != null) {
                try {
                    localCnx.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }
            
            String errorCode = "DB-UPDATE-" + (e.getErrorCode() != 0 ? e.getErrorCode() : "UNKNOWN");
            System.err.println(errorCode + ": Erreur lors de la modification de la demande : " + e.getMessage());
            throw new SQLException("Erreur de modification de demande [" + errorCode + "]: " + e.getMessage(), e);
        } finally {
            // Fermeture des ressources
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du statement : " + e.getMessage());
                }
            }
            
            // Rétablir l'autocommit
            if (localCnx != null) {
                try {
                    localCnx.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Erreur lors du rétablissement de l'autocommit : " + e.getMessage());
                }
            }
        }
    }
    @Override
    public void supprimer(int id_demande) throws SQLException {
        if (id_demande <= 0) {
            throw new SQLException("L'ID de la demande doit être un nombre positif pour la suppression");
        }
        
        Connection localCnx = null;
        PreparedStatement preparedStatement = null;
        
        try {
            // Ouvrir une transaction locale
            localCnx = MyDataBase.getInstance().getCnx();
            localCnx.setAutoCommit(false);
            
            String sql = "DELETE FROM demande WHERE id_demande = ?";
            preparedStatement = localCnx.prepareStatement(sql);
            preparedStatement.setInt(1, id_demande);
            
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                // Commit de la transaction
                localCnx.commit();
                System.out.println("Demande supprimée avec succès. ID: " + id_demande);
            } else {
                // Rollback en cas d'échec
                localCnx.rollback();
                throw new SQLException("Aucune demande supprimée. Vérifiez l'ID: " + id_demande);
            }
        } catch (SQLException e) {
            // Rollback en cas d'exception
            if (localCnx != null) {
                try {
                    localCnx.rollback();
                } catch (SQLException ex) {
                    System.err.println("Erreur lors du rollback : " + ex.getMessage());
                }
            }
            
            String errorCode = "DB-DELETE-" + (e.getErrorCode() != 0 ? e.getErrorCode() : "UNKNOWN");
            System.err.println(errorCode + ": Erreur lors de la suppression de la demande : " + e.getMessage());
            throw new SQLException("Erreur de suppression de demande [" + errorCode + "]: " + e.getMessage(), e);
        } finally {
            // Fermeture des ressources
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du statement : " + e.getMessage());
                }
            }
            
            // Rétablir l'autocommit
            if (localCnx != null) {
                try {
                    localCnx.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Erreur lors du rétablissement de l'autocommit : " + e.getMessage());
                }
            }
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


    /**
     * Met à jour le nom dans la demande en se basant sur l'utilisateur associé
     * @param demande La demande à mettre à jour
     * @throws SQLException En cas d'erreur SQL
     * @throws IllegalArgumentException Si la demande est null
     */
    public void setUserNameInDemande(Demande demande) throws SQLException {
        if (demande == null) {
            throw new IllegalArgumentException("La demande ne peut pas être null");
        }
        
        Utilisateur utilisateur = getUtilisateurById(demande.getId_user());
        if (utilisateur != null) {
            demande.setNom(utilisateur.getNom() + " " + utilisateur.getPrenom());
        } else {
            System.err.println("Attention: Utilisateur avec ID " + demande.getId_user() + " non trouvé");
        }
    }
    /**
     * Récupère un utilisateur par son ID
     * @param idUser L'ID de l'utilisateur à récupérer
     * @return L'utilisateur correspondant ou null si non trouvé
     * @throws SQLException En cas d'erreur SQL
     */
    private Utilisateur getUtilisateurById(int idUser) throws SQLException {
        if (idUser <= 0) {
            throw new IllegalArgumentException("L'ID utilisateur doit être un nombre positif");
        }
        
        String sql = "SELECT * FROM utilisateur WHERE id_user = ?";
        try (PreparedStatement preparedStatement = cnx.prepareStatement(sql)) {
            preparedStatement.setInt(1, idUser);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId(resultSet.getInt("id_user"));
                    utilisateur.setNom(resultSet.getString("nom_user"));
                    utilisateur.setPrenom(resultSet.getString("prenom_user"));
                    utilisateur.setEmail(resultSet.getString("email"));
                    utilisateur.setMotDePasse(resultSet.getString("motDePasse"));
                    
                    // Gestion sécurisée de l'enum Role
                    String roleStr = resultSet.getString("role");
                    try {
                        if (roleStr != null && !roleStr.isEmpty()) {
                            utilisateur.setRole(Role.valueOf(roleStr.toUpperCase()));
                        } else {
                            // Valeur par défaut si la colonne est vide
                            utilisateur.setRole(Role.CITOYEN);
                        }
                    } catch (IllegalArgumentException e) {
                        System.err.println("Rôle invalide dans la base de données: " + roleStr);
                        // Valeur par défaut en cas d'erreur
                        utilisateur.setRole(Role.CITOYEN);
                    }
                    
                    utilisateur.setActif(resultSet.getBoolean("actif"));
                    
                    // Gestion sécurisée de la date de création
                    Timestamp dateCreation = resultSet.getTimestamp("date_creation");
                    if (dateCreation != null) {
                        // Si vous avez une méthode pour définir la date de création, utilisez-la ici
                        // utilisateur.setDateCreation(dateCreation.toLocalDateTime());
                    }
                    
                    return utilisateur;
                }
            }
        } catch (SQLException e) {
            String errorCode = "DB-SELECT-" + (e.getErrorCode() != 0 ? e.getErrorCode() : "UNKNOWN");
            System.err.println(errorCode + ": Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
            throw new SQLException("Erreur de récupération d'utilisateur [" + errorCode + "]: " + e.getMessage(), e);
        }
        return null;
    }
}
