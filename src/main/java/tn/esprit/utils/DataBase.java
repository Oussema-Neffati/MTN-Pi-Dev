package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataBase {

    private static DataBase instance;
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/mte";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private Connection cnx;
    private static final Logger LOGGER = Logger.getLogger(DataBase.class.getName());

    private DataBase() {
        try {
            // Load MySQL JDBC driver explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            LOGGER.info("Connected to the database.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Connection failed: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found: " + e.getMessage(), e);
        }
    }

    public static DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    public Connection Cnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                    LOGGER.info("Reconnected to the database.");
                } catch (SQLException | ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, "Reconnection failed: " + e.getMessage(), e);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking connection: " + e.getMessage(), e);
        }
        return cnx;
    }

    public static Connection getCnx() {
        return getInstance().Cnx(); // Fixed: Changed getCnx() to Cnx()
    }
}