package org.example.test;

import org.example.models.User;
import org.example.repositories.UserRepository;
import org.example.util.DatabaseUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository userRepository;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private MockedStatic<DatabaseUtil> mockedStatic;

    @BeforeEach
    void setUp() throws Exception {
        // Mock dependencies
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        // Mock DatabaseUtil to return the mocked connection
        mockedStatic = mockStatic(DatabaseUtil.class);
        when(DatabaseUtil.getConnection()).thenReturn(mockConnection);

        // Mock PreparedStatement and ResultSet behavior
        when(mockConnection.prepareStatement(any(String.class))).thenReturn(mockStatement);

        userRepository = new UserRepository();
    }

    @AfterEach
    void tearDown() {
        if (mockedStatic != null) {
            mockedStatic.close();
        }
    }

    // Test 1: Successfully retrieves a user by username
    @Test
    void testGetUserByUsernameSuccess() throws Exception {
        // Arrange
        String username = "testUser";
        UUID userId = UUID.randomUUID();

        // Mock result set data - User class uses UUID for id, not int
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getObject("id", UUID.class)).thenReturn(userId);
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
        assertEquals(userId, result.getId()); // UUID comparison
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

        // Mock executeQuery to return empty result set for null username
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // No user found

        // Act
        User result = userRepository.getUserByUsername(username);

        // Assert - the method returns null when no user is found
        assertNull(result);

        // executeQuery is called with null (the method doesn't validate before executing)
        verify(mockStatement).setString(1, null);
        verify(mockStatement, times(1)).executeQuery();
    }
}