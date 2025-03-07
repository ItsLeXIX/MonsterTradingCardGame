package org.example.test;

import org.example.controllers.UserController;
import org.example.dtos.RegisterRequest;
import org.example.dtos.LoginRequest;
import org.example.dtos.AuthResponse;
import org.example.models.User;
import org.example.repositories.CardRepository;
import org.example.services.UserService;
import org.example.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.UUID;

class UserControllerTest {

    private UserController userController;
    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Mock dependencies
        userRepository = mock(UserRepository.class);
        CardRepository cardRepository = mock(CardRepository.class); // Add this line
        userService = new UserService(userRepository);
        userController = new UserController(userService, userRepository, cardRepository); // Update here
    }

    // Test 1: Register a new user successfully
    @Test
    void testRegisterUserSuccess() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setPassword("password");
        User user = new User(UUID.randomUUID(), "testUser", "password", "Test Name", "Bio", "image.png", 20, 1000, 0, 0);
        when(userRepository.createUser(any(User.class))).thenReturn(true);

        // Act
        AuthResponse response = userController.register(request);

        // Assert
        assertTrue(response.getMessage().contains("successfully"));
    }

    // Test 2: Register with duplicate username
    @Test
    void testRegisterUserDuplicate() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setPassword("password");
        User user = new User(UUID.randomUUID(), "existingUser", "password", "Test Name", "Bio", "image.png", 20, 1000, 0, 0);
        when(userRepository.createUser(any(User.class))).thenReturn(false);

        // Act
        AuthResponse response = userController.register(request);

        // Assert
        assertFalse(response.getMessage().contains("successfully"));
    }

    // Test 3: Login with valid credentials
    @Test
    void testLoginUserSuccess() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testUser");
        request.setPassword("password");
        User mockUser = new User(UUID.randomUUID(), "testUser", "password", "Test Name", "Bio", "image.png", 20, 1000, 0, 0);
        when(userRepository.getUserByUsername("testUser")).thenReturn(mockUser);

        // Act
        AuthResponse response = userController.login(request);

        // Assert
        assertTrue(response.getMessage().contains("Token"));
    }

    // Test 4: Login with invalid credentials
    @Test
    void testLoginUserInvalidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testUser");
        request.setPassword("password");
        User mockUser = new User(UUID.randomUUID(), "testUser", "password", "Test Name", "Bio", "image.png", 20, 1000, 0, 0);
        when(userRepository.getUserByUsername("testUser")).thenReturn(mockUser);

        // Act
        AuthResponse response = userController.login(request);

        // Assert
        assertFalse(response.getMessage().contains("Token"));
    }
}