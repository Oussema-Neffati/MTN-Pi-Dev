package tn.esprit.services;

import tn.esprit.interfaces.IService;
import tn.esprit.models.Reservation;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.List;

public class ServiceReservation implements IService<Reservation> {
    private Connection cnx;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY = 1000; // milliseconds

    public ServiceReservation() {
        initConnection();
        // Debug: Print direct database and table information
        try {
            Statement stmt = cnx.createStatement();

            // First print the current database name
            ResultSet rs = stmt.executeQuery("SELECT DATABASE()");
            if (rs.next()) {
                System.out.println("\n======== DIRECT DATABASE DIAGNOSTICS ========");
                System.out.println("Connected to database: " + rs.getString(1));
            }

            // Then show all tables
            rs = stmt.executeQuery("SHOW TABLES");
            System.out.println("\nTables in database:");
            boolean hasTables = false;
            while (rs.next()) {
                hasTables = true;
                System.out.println(" - " + rs.getString(1));
            }
            if (!hasTables) {
                System.out.println(" (No tables found in database)");
            }

            // Finally show the reservation table structure using multiple methods
            try {
                System.out.println("\nReservation table structure via DESCRIBE:");
                rs = stmt.executeQuery("DESCRIBE `reservation`");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString("Field") + " (" + rs.getString("Type") + ")");
                }
            } catch (SQLException e) {
                System.out.println("Error getting DESCRIBE: " + e.getMessage());
            }

            try {
                System.out.println("\nReservation table structure via SHOW COLUMNS:");
                rs = stmt.executeQuery("SHOW COLUMNS FROM `reservation`");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString("Field") + " (" + rs.getString("Type") + ")");
                }
            } catch (SQLException e) {
                System.out.println("Error getting SHOW COLUMNS: " + e.getMessage());
            }

            try {
                System.out.println("\nReservation table CREATE statement:");
                rs = stmt.executeQuery("SHOW CREATE TABLE `reservation`");
                if (rs.next()) {
                    System.out.println(rs.getString(2));
                }
            } catch (SQLException e) {
                System.out.println("Error getting CREATE TABLE: " + e.getMessage());
            }

            System.out.println("=========================================\n");
        } catch (SQLException e) {
            System.err.println("Error during direct database diagnostics: " + e.getMessage());
            e.printStackTrace();
        }

        // Also use the detailed diagnostic method
        debugPrintTableStructure("reservation");
    }

    private void initConnection() {
        try {
            // Get connection from MyDataBase singleton
            MyDataBase instance = MyDataBase.getInstance(); // This will ensure tables are created
            cnx = instance.getCnx();

            if (cnx == null || !cnx.isValid(2)) {
                throw new SQLException("Could not establish database connection");
            }

            // Create/verify table structure
            String createTableSQL =
                "CREATE TABLE IF NOT EXISTS `reservation` (\n" +
                "  `id_res` INT AUTO_INCREMENT PRIMARY KEY,\n" +
                "  `date_reservation` DATE NOT NULL,\n" +
                "  `heure_debut` VARCHAR(10) NOT NULL,\n" +
                "  `heure_fin` VARCHAR(10) NOT NULL,\n" +
                "  `status` VARCHAR(50) NOT NULL,\n" +
                "  `nombre_participants` INT NOT NULL,\n" +
                "  `motif` TEXT NOT NULL,\n" +
                "  `id_utilisateur` INT NOT NULL,\n" +
                "  `id_ressource` INT NOT NULL,\n" +
                "  INDEX `idx_utilisateur` (`id_utilisateur`),\n" +
                "  INDEX `idx_ressource` (`id_ressource`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci";

            try (Statement stmt = cnx.createStatement()) {
                stmt.execute(createTableSQL);
                System.out.println("ReservationService: Table structure verified/created successfully");
            }

            System.out.println("ReservationService: Database connection initialized successfully");
        } catch (SQLException e) {
            System.err.println("ReservationService: Failed to initialize connection: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    /**
     * Ensure connection is valid before performing operations
     *
     * @throws SQLException if connection cannot be established
     */
    private void ensureConnection() throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            System.out.println("ReservationService: Connection is null or closed, reinitializing...");
            initConnection();

            if (cnx == null) {
                throw new SQLException("Cannot establish database connection");
            }
        }
    }

    @Override
    public void add(Reservation reservation) {
        try {
            ensureConnection();

            String qry = "INSERT INTO `reservation` (`date_reservation`, `heure_debut`, `heure_fin`, `status`, "
                        + "`nombre_participants`, `motif`, `id_utilisateur`, `id_ressource`) "
                        + "VALUES (?,?,?,?,?,?,?,?)";

            System.out.println("Executing query: " + qry);
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setDate(1, java.sql.Date.valueOf(reservation.getDateReservation()));
            pstm.setString(2, reservation.getHeureDebut());
            pstm.setString(3, reservation.getHeureFin());
            pstm.setString(4, reservation.getStatus());
            pstm.setInt(5, reservation.getNombreParticipants());
            pstm.setString(6, reservation.getMotif());
            pstm.setInt(7, reservation.getIdUtilisateur());
            pstm.setInt(8, reservation.getIdRessource());
            pstm.executeUpdate();
            System.out.println("Insert successful!");
        } catch (SQLException e) {
            System.err.println("Error adding reservation: " + e.getMessage());
            throw new RuntimeException("Failed to add reservation", e);
        }
    }

    @Override
    public List<Reservation> getAll() {
        List<Reservation> reservations = new ArrayList<>();
        String qry = "SELECT * FROM `reservation`";
        try {
            ensureConnection();
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setIdRes(rs.getInt("id_res"));
                r.setDateReservation(rs.getDate("date_reservation").toLocalDate());
                r.setHeureDebut(rs.getString("heure_debut"));
                r.setHeureFin(rs.getString("heure_fin"));
                r.setStatus(rs.getString("status"));
                r.setNombreParticipants(rs.getInt("nombre_participants"));
                r.setMotif(rs.getString("motif"));
                r.setIdUtilisateur(rs.getInt("id_utilisateur"));
                r.setIdRessource(rs.getInt("id_ressource"));
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving reservations: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve reservations", e);
        }
        return reservations;
    }

    @Override
    public void update(Reservation reservation) {
        String qry = "UPDATE `reservation` SET `date_reservation`=?, `heure_debut`=?, `heure_fin`=?, `status`=?, " +
                    "`nombre_participants`=?, `motif`=?, `id_utilisateur`=?, `id_ressource`=? WHERE `id_res`=?";
        try {
            ensureConnection();
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setDate(1, java.sql.Date.valueOf(reservation.getDateReservation()));
            pstm.setString(2, reservation.getHeureDebut());
            pstm.setString(3, reservation.getHeureFin());
            pstm.setString(4, reservation.getStatus());
            pstm.setInt(5, reservation.getNombreParticipants());
            pstm.setString(6, reservation.getMotif());
            pstm.setInt(7, reservation.getIdUtilisateur());
            pstm.setInt(8, reservation.getIdRessource());
            pstm.setInt(9, reservation.getIdRes());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating reservation: " + e.getMessage());
            throw new RuntimeException("Failed to update reservation", e);
        }
    }

    @Override
    public void delete(Reservation reservation) {
        String qry = "DELETE FROM `reservation` WHERE `id_res`=?";
        try {
            ensureConnection();
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, reservation.getIdRes());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting reservation: " + e.getMessage());
            throw new RuntimeException("Failed to delete reservation", e);
        }
    }

    // Get reservations by user ID
    public List<Reservation> getByUserId(int userId) {
        List<Reservation> reservations = new ArrayList<>();
        String qry = "SELECT * FROM `reservation` WHERE `id_utilisateur`=?";
        try {
            ensureConnection();
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, userId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setIdRes(rs.getInt("id_res"));
                r.setDateReservation(rs.getDate("date_reservation").toLocalDate());
                r.setHeureDebut(rs.getString("heure_debut"));
                r.setHeureFin(rs.getString("heure_fin"));
                r.setStatus(rs.getString("status"));
                r.setNombreParticipants(rs.getInt("nombre_participants"));
                r.setMotif(rs.getString("motif"));
                r.setIdUtilisateur(rs.getInt("id_utilisateur"));
                r.setIdRessource(rs.getInt("id_ressource"));
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving reservations by user ID: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve reservations by user ID", e);
        }
        return reservations;
    }

    // Get reservations by resource ID
    public List<Reservation> getByRessourceId(int ressourceId) {
        List<Reservation> reservations = new ArrayList<>();
        String qry = "SELECT * FROM `reservation` WHERE `id_ressource`=?";
        try {
            ensureConnection();
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, ressourceId);
            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setIdRes(rs.getInt("id_res"));
                r.setDateReservation(rs.getDate("date_reservation").toLocalDate());
                r.setHeureDebut(rs.getString("heure_debut"));
                r.setHeureFin(rs.getString("heure_fin"));
                r.setStatus(rs.getString("status"));
                r.setNombreParticipants(rs.getInt("nombre_participants"));
                r.setMotif(rs.getString("motif"));
                r.setIdUtilisateur(rs.getInt("id_utilisateur"));
                r.setIdRessource(rs.getInt("id_ressource"));
                reservations.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving reservations by resource ID: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve reservations by resource ID", e);
        }
        return reservations;
    }

    // Check for resource availability
    public boolean isResourceAvailable(int resourceId, Date date, String startTime, String endTime) {
        String qry = "SELECT COUNT(*) FROM `reservation` WHERE `id_ressource`=? AND `date_reservation`=? " +
                "AND ((`heure_debut` <= ? AND `heure_fin` > ?) OR (`heure_debut` < ? AND `heure_fin` >= ?)) " +
                "AND `status` != 'ANNULEE'";
        try {
            ensureConnection();
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, resourceId);
            pstm.setDate(2, date);
            pstm.setString(3, endTime);
            pstm.setString(4, startTime);
            pstm.setString(5, endTime);
            pstm.setString(6, startTime);
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // If count is 0, resource is available
            }
        } catch (SQLException e) {
            System.err.println("Error checking resource availability: " + e.getMessage());
            throw new RuntimeException("Failed to check resource availability", e);
        }
        return false;
    }

    /**
     * Debug method to print the actual table structure from the database
     * @param tableName the name of the table to examine
     */
    public void debugPrintTableStructure(String tableName) {
        try {
            ensureConnection();

            // Print current database name
            try {
                Statement stmt = cnx.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT DATABASE()");
                if (rs.next()) {
                    System.out.println("\n==== DEBUG: Current Database ====");
                    System.out.println("Connected to database: " + rs.getString(1));
                    System.out.println("================================\n");
                }
            } catch (SQLException e) {
                System.out.println("Could not get current database: " + e.getMessage());
            }

            // First try SHOW CREATE TABLE
            try {
                Statement stmt = cnx.createStatement();
                ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE `" + tableName + "`");
                if (rs.next()) {
                    System.out.println("\n==== DEBUG: Table Creation SQL ====");
                    System.out.println(rs.getString(2));
                    System.out.println("================================\n");
                }
            } catch (SQLException e) {
                System.out.println("Could not get CREATE TABLE statement: " + e.getMessage());

                // Try to list all tables in the database to see what's available
                try {
                    Statement stmt = cnx.createStatement();
                    ResultSet rs = stmt.executeQuery("SHOW TABLES");
                    System.out.println("\n==== DEBUG: Tables in database ====");
                    boolean hasAnyTables = false;
                    while (rs.next()) {
                        hasAnyTables = true;
                        System.out.println(" - " + rs.getString(1));
                    }
                    if (!hasAnyTables) {
                        System.out.println(" (No tables found)");
                    }
                    System.out.println("================================\n");
                } catch (SQLException ex) {
                    System.out.println("Could not list tables: " + ex.getMessage());
                }
            }

            // Then get column information using metadata
            DatabaseMetaData metaData = cnx.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);

            if (!columns.next()) {
                System.out.println("\n==== DEBUG: Table '" + tableName + "' structure ====");
                System.out.println("WARNING: Table '" + tableName + "' does not exist or has no columns!");
                System.out.println("================================\n");
                return;
            }

            // Reset cursor
            columns.beforeFirst();

            // Print column information
            System.out.println("\n==== DEBUG: Column Information ====");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String columnType = columns.getString("TYPE_NAME");
                String columnSize = columns.getString("COLUMN_SIZE");
                String isNullable = columns.getString("IS_NULLABLE");

                System.out.println(" - " + columnName + " (" + columnType +
                                   ", size=" + columnSize +
                                   ", nullable=" + isNullable + ")");
            }
            System.out.println("================================\n");

            // Print connection details
            System.out.println("\n==== DEBUG: Connection Details ====");
            System.out.println("Database URL: " + cnx.getMetaData().getURL());
            System.out.println("Database User: " + cnx.getMetaData().getUserName());
            System.out.println("Driver Name: " + cnx.getMetaData().getDriverName());
            System.out.println("Driver Version: " + cnx.getMetaData().getDriverVersion());
            System.out.println("================================\n");

        } catch (SQLException e) {
            System.err.println("Error examining table structure: " + e.getMessage());
            e.printStackTrace();

            // Try to print connection details even if there's an error
            try {
                System.out.println("\n==== DEBUG: Connection Details (on error) ====");
                System.out.println("Database URL: " + cnx.getMetaData().getURL());
                System.out.println("Database User: " + cnx.getMetaData().getUserName());
                System.out.println("================================\n");
            } catch (SQLException ex) {
                System.err.println("Could not get connection details: " + ex.getMessage());
            }
        }
    }
}
