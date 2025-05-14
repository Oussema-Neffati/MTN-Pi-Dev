package tn.esprit.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class MyDataBase {
    private static MyDataBase instance;
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/base_commune";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private HikariDataSource dataSource;

    private MyDataBase() {
        initializeDataSource();
    }

    private void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);
            
            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000); // 5 minutes
            config.setConnectionTimeout(20000); // 20 seconds
            config.setAutoCommit(true);
            
            // MySQL specific settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            dataSource = new HikariDataSource(config);
            System.out.println("Connection pool initialized successfully.");
        } catch (Exception e) {
            System.err.println("Error initializing connection pool: " + e.getMessage());
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
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Error getting connection from pool: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Cleanup method to be called when shutting down the application
    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
