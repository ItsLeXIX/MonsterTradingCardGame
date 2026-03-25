package org.example.test;

import org.example.models.Package;
import org.example.repositories.PackageRepository;
import org.example.util.DatabaseUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PackageRepositoryTest {

    private PackageRepository packageRepository;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DatabaseUtil> mockedStatic;

    @BeforeEach
    void setUp() throws Exception {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        mockedStatic = mockStatic(DatabaseUtil.class);
        when(DatabaseUtil.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        packageRepository = new PackageRepository();
    }

    @AfterEach
    void tearDown() {
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    @Test
    void testAddPackageSuccess() throws SQLException {
        // Arrange
        List<String> cardIds = new ArrayList<>();
        cardIds.add(UUID.randomUUID().toString());
        cardIds.add(UUID.randomUUID().toString());

        Package pkg = new Package(UUID.randomUUID(), cardIds, 100);

        // Mock behavior - first statement returns 1 row affected
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Need to mock the second prepared statement for linking cards
        PreparedStatement mockLinkStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement, mockLinkStatement);
        when(mockLinkStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = packageRepository.addPackage(pkg);

        // Assert
        assertTrue(result);

        // Verify package insert was attempted
        verify(mockStatement).executeUpdate();
    }

    @Test
    void testAddPackageSQLException() throws SQLException {
        // Arrange
        List<String> cardIds = new ArrayList<>();
        cardIds.add(UUID.randomUUID().toString());

        Package pkg = new Package(UUID.randomUUID(), cardIds, 100);

        // Simulate SQL Exception
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("SQL error"));

        // Act
        boolean result = packageRepository.addPackage(pkg);

        // Assert
        assertFalse(result);
    }
}