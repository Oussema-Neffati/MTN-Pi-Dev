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

    private static Connection cnx;
    private static final Logger LOGGER = Logger.getLogger(DataBase.class.getName());

    private DataBase() {
        try {
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            LOGGER.info("Connected to the database.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Connection failed: " + e.getMessage(), e);
        }
    }

    public static DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    public static Connection getCnx() {
        if (cnx == null) {
            LOGGER.warning("Database connection is not established.");
        }
        return cnx;
    }
}
