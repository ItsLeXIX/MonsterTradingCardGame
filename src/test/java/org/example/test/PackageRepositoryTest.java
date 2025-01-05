package org.example.test;

import org.example.models.Package;
import org.example.repositories.PackageRepository;
import org.example.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PackageRepositoryTest {

    private PackageRepository packageRepository;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet; // Mock ResultSet

    @BeforeEach
    void setUp() throws Exception {
        // Mock dependencies
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class); // Mock ResultSet

        // Mock DatabaseUtil to return mockConnection
        mockStatic(DatabaseUtil.class);
        when(DatabaseUtil.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet); // Mock query result
    }

    // Test for successful package creation
    @Test
    void testAddPackageSuccess() throws SQLException {
        // Arrange
        List<String> cardIds = new ArrayList<>();
        cardIds.add("card1");
        cardIds.add("card2");
        Package pkg = new Package(1, cardIds, 100);

        // Mock ResultSet behavior
        when(mockResultSet.next()).thenReturn(true);  // Simulate one row exists
        when(mockResultSet.getInt(1)).thenReturn(1); // Mock returned ID

        // Create PackageRepository
        packageRepository = new PackageRepository();

        // Act
        boolean result = packageRepository.addPackage(pkg);

        // Assert
        assertTrue(result);

        // Verify SQL parameters were set correctly
        verify(mockStatement).setInt(1, 1);  // Package ID
        verify(mockStatement).setInt(2, 100); // Price
        verify(mockStatement).executeQuery(); // Ensure query execution
    }

    // Test for failure due to SQLException
    @Test
    void testAddPackageSQLException() throws SQLException {
        // Arrange
        List<String> cardIds = new ArrayList<>();
        cardIds.add("card1");
        Package pkg = new Package(1, cardIds, 100);

        // Simulate SQL Exception
        when(mockStatement.executeQuery()).thenThrow(new SQLException("SQL error"));

        // Create PackageRepository
        packageRepository = new PackageRepository();

        // Act
        boolean result = packageRepository.addPackage(pkg);

        // Assert
        assertFalse(result); // Expect failure
    }
}