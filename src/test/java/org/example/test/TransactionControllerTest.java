package org.example.test;

import org.example.models.User;
import org.example.repositories.UserRepository;
import org.example.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws Exception {
        // Mock dependencies
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mock DatabaseUtil to return the mocked connection
        mockStatic(DatabaseUtil.class);
        when(DatabaseUtil.getConnection()).thenReturn(mockConnection);

        // Mock PreparedStatement and ResultSet behavior
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);

        userRepository = new UserRepository();
    }

    // Test 1: Successfully retrieves a user by username
    @Test
    void testGetUserByUsernameSuccess() throws Exception {
        // Arrange
        String username = "testUser";

        // Mock result set data
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn("testUser");
        when(mockResultSet.getString("password")).thenReturn("password123");
        when(mockResultSet.getString("name")).thenReturn("Test Name");
        when(mockResultSet.getString("bio")).thenReturn("Test Bio");
        when(mockResultSet.getString("image")).thenReturn("image.png");
        when(mockResultSet.getInt("coins")).thenReturn(20);
        when(mockResultSet.getInt("elo")).thenReturn(1000);
        when(mockResultSet.getInt("wins")).thenReturn(10);
        when(mockResultSet.getInt("losses")).thenReturn(5);

        // Act
        User result = userRepository.getUserByUsername(username);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("testUser", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertEquals("Test Name", result.getName());
        assertEquals("Test Bio", result.getBio());
        assertEquals("image.png", result.getImage());
        assertEquals(20, result.getCoins());
        assertEquals(1000, result.getElo());
        assertEquals(10, result.getWins());
        assertEquals(5, result.getLosses());

        // Verify SQL execution
        verify(mockStatement).setString(1, username);
        verify(mockStatement, times(1)).executeQuery();
    }

    // Test 2: User not found
    @Test
    void testGetUserByUsernameNotFound() throws Exception {
        // Arrange
        String username = "unknownUser";

        // Simulate no user found
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        User result = userRepository.getUserByUsername(username);

        // Assert
        assertNull(result); // Expect null for non-existing user

        // Verify SQL execution
        verify(mockStatement).setString(1, username);
        verify(mockStatement, times(1)).executeQuery();
    }

    // Test 3: SQL Exception occurs
    @Test
    void testGetUserByUsernameSQLException() throws Exception {
        // Arrange
        String username = "errorUser";

        // Simulate SQL Exception
        when(mockStatement.executeQuery()).thenThrow(new SQLException("Database Error"));

        // Act
        User result = userRepository.getUserByUsername(username);

        // Assert
        assertNull(result); // Expect null when exception occurs

        // Verify SQL execution
        verify(mockStatement).setString(1, username);
        verify(mockStatement, times(1)).executeQuery();
    }

    // Test 4: Empty username input
    @Test
    void testGetUserByUsernameEmptyInput() throws Exception {
        // Arrange
        String username = ""; // Empty string

        // Simulate no user found
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        User result = userRepository.getUserByUsername(username);

        // Assert
        assertNull(result); // Expect null for invalid input

        // Verify SQL execution
        verify(mockStatement).setString(1, username);
        verify(mockStatement, times(1)).executeQuery();
    }

    // Test 5: Null username input
    @Test
    void testGetUserByUsernameNullInput() throws Exception {
        // Arrange
        String username = null; // Null username

        // Act & Assert
        assertThrows(NullPointerException.class, () -> userRepository.getUserByUsername(username));

        // Verify no SQL execution occurred due to null input
        verify(mockStatement, never()).executeQuery();
    }
}