package org.example.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {

    // Database connection details
    private static final String URL = "jdbc:postgresql://localhost:5432/mctg";
    private static final String USER = "postgres";
    private static final String PASSWORD = "";

    // Load PostgreSQL driver
    static {
        try {
            Class.forName("org.postgresql.Driver"); // Ensure driver is loaded
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found!", e);
        }
    }

    // Connect to the database
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);

        // Log the database URL for debugging
        System.out.println("Connected to database: " + conn.getMetaData().getURL());
        return conn; // Return the connection object
    }

    // Test database connection
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database.");
            e.printStackTrace();
        }
    }
}