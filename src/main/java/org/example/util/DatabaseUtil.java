package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    // Database connection details
    private static final String URL = "jdbc:postgresql://localhost:5432/mctg";
    private static final String USER = "postgres";  // PostgreSQL user
    private static final String PASSWORD = "";     // No password due to 'trust'

    // Load PostgreSQL driver
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found!", e);
        }
    }

    // Connect to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}