package tn.esprit.services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import tn.esprit.models.Ressource;
import tn.esprit.models.Ressource;
import tn.esprit.utils.MyDataBase;

/**
 * Service class for handling CRUD operations for Ressource entities
 */
public class ServiceRessource{

    private Connection connection;

    /**
     * Constructor initializes database connection
     */
    public ServiceRessource() {
        connection =MyDataBase.getInstance().getCnx();
    }

    /**
     * Inserts a new resource into the database
     * @param ressource The resource to insert
     * @return The inserted resource with generated ID
     * @throws SQLException If a database error occurs
     */
    public Ressource insert(Ressource ressource) throws SQLException {
        String query = "INSERT INTO ressources (nom, categorie, capacite, tarif_horaire, " +
                "horaire_ouverture, horaire_fermeture, description, disponible) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ressource.getNom());
            ps.setString(2, ressource.getCategorie());
            ps.setInt(3, ressource.getCapacite());
            ps.setDouble(4, ressource.getTarifHoraire());
            ps.setString(5, ressource.getHoraireOuverture());
            ps.setString(6, ressource.getHoraireFermeture());
            ps.setString(7, ressource.getDescription());
            ps.setBoolean(8, ressource.isDisponible());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création de la ressource a échoué, aucune ligne ajoutée.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ressource.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création de la ressource a échoué, aucun ID obtenu.");
                }
            }
        }

        return ressource;
    }

    /**
     * Updates an existing resource in the database
     * @param ressource The resource to update
     * @return True if the update was successful
     * @throws SQLException If a database error occurs
     */
    public boolean update(Ressource ressource) throws SQLException {
        String query = "UPDATE ressources SET nom = ?, categorie = ?, capacite = ?, " +
                "tarif_horaire = ?, horaire_ouverture = ?, horaire_fermeture = ?, " +
                "description = ?, disponible = ? WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, ressource.getNom());
            ps.setString(2, ressource.getCategorie());
            ps.setInt(3, ressource.getCapacite());
            ps.setDouble(4, ressource.getTarifHoraire());
            ps.setString(5, ressource.getHoraireOuverture());
            ps.setString(6, ressource.getHoraireFermeture());
            ps.setString(7, ressource.getDescription());
            ps.setBoolean(8, ressource.isDisponible());
            ps.setInt(9, ressource.getId());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Deletes a resource from the database by its ID
     * @param id The ID of the resource to delete
     * @return True if the deletion was successful
     * @throws SQLException If a database error occurs
     */
    public boolean delete(int id) throws SQLException {
        String query = "DELETE FROM ressources WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Reads a resource from the database by its ID
     * @param id The ID of the resource to read
     * @return The resource if found, null otherwise
     * @throws SQLException If a database error occurs
     */
    public Ressource readById(int id) throws SQLException {
        String query = "SELECT * FROM ressources WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractRessourceFromResultSet(rs);
                }
            }
        }

        return null;
    }

    /**
     * Reads all resources from the database
     * @return A list of all resources
     * @throws SQLException If a database error occurs
     */
    public List<Ressource> readAll() throws SQLException {
        List<Ressource> ressources = new ArrayList<>();
        String query = "SELECT * FROM ressources";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Ressource ressource = extractRessourceFromResultSet(rs);
                ressources.add(ressource);
            }
        }

        return ressources;
    }

    /**
     * Searches for resources by name, category, or description
     * @param keyword The keyword to search for
     * @return A list of matching resources
     * @throws SQLException If a database error occurs
     */
    public List<Ressource> searchRessources(String keyword) throws SQLException {
        List<Ressource> ressources = new ArrayList<>();
        String query = "SELECT * FROM ressources WHERE nom LIKE ? OR categorie LIKE ? OR description LIKE ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ressource ressource = extractRessourceFromResultSet(rs);
                    ressources.add(ressource);
                }
            }
        }

        return ressources;
    }

    /**
     * Filters resources by category
     * @param category The category to filter by
     * @return A list of resources in the specified category
     * @throws SQLException If a database error occurs
     */
    public List<Ressource> filterByCategory(String category) throws SQLException {
        List<Ressource> ressources = new ArrayList<>();
        String query = "SELECT * FROM ressources WHERE categorie = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, category);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ressource ressource = extractRessourceFromResultSet(rs);
                    ressources.add(ressource);
                }
            }
        }

        return ressources;
    }

    /**
     * Filters resources by availability
     * @param disponible The availability status to filter by
     * @return A list of resources with the specified availability
     * @throws SQLException If a database error occurs
     */
    public List<Ressource> filterByAvailability(boolean disponible) throws SQLException {
        List<Ressource> ressources = new ArrayList<>();
        String query = "SELECT * FROM ressources WHERE disponible = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setBoolean(1, disponible);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ressource ressource = extractRessourceFromResultSet(rs);
                    ressources.add(ressource);
                }
            }
        }

        return ressources;
    }

    /**
     * Helper method to extract a Ressource object from a ResultSet
     * @param rs The ResultSet to extract from
     * @return The extracted Ressource
     * @throws SQLException If a database error occurs
     */
    private Ressource extractRessourceFromResultSet(ResultSet rs) throws SQLException {
        Ressource ressource = new Ressource();

        ressource.setId(rs.getInt("id"));
        ressource.setNom(rs.getString("nom"));
        ressource.setCategorie(rs.getString("categorie"));
        ressource.setCapacite(rs.getInt("capacite"));
        ressource.setTarifHoraire(rs.getDouble("tarif_horaire"));
        ressource.setHoraireOuverture(rs.getString("horaire_ouverture"));
        ressource.setHoraireFermeture(rs.getString("horaire_fermeture"));
        ressource.setDescription(rs.getString("description"));
        ressource.setDisponible(rs.getBoolean("disponible"));

        return ressource;}}


