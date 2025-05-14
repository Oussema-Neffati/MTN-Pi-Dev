package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyDataBase {
    private final String URL = "jdbc:mysql://127.0.0.1:3306/base_commune";
    private final String USER = "root";
    private final String PASSWORD = "";
    private Connection cnx;
    private static MyDataBase instance;

    private MyDataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to Database!");
            initializeTables();
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void initializeTables() {
        try {
            Statement stmt = cnx.createStatement();
            
            // Create reservation table
            String createReservationTable = "CREATE TABLE IF NOT EXISTS reservation (" +
                "idRes INT AUTO_INCREMENT PRIMARY KEY, " +
                "dateReservation DATE NOT NULL, " +
                "heureDebut VARCHAR(10) NOT NULL, " +
                "heureFin VARCHAR(10) NOT NULL, " +
                "status VARCHAR(50) NOT NULL, " +
                "nombreParticipants INT NOT NULL, " +
                "motif TEXT NOT NULL, " +
                "cin VARCHAR(8) NOT NULL, " +
                "idUtilisateur INT NOT NULL, " +
                "idRessource INT NOT NULL, " +
                "INDEX idx_utilisateur (idUtilisateur), " +
                "INDEX idx_ressource (idRessource)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci";
            
            stmt.executeUpdate(createReservationTable);
            System.out.println("Reservation table created or already exists");
            
        } catch (SQLException e) {
            System.err.println("Error initializing tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
