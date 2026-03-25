package org.example.test;

import org.example.controllers.UserController;
import org.example.dtos.AuthResponse;
import org.example.dtos.LoginRequest;
import org.example.dtos.RegisterRequest;
import org.example.repositories.CardRepository;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userController = new UserController(userService, userRepository, cardRepository);
    }

    @Test
    void testRegisterUserSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setPassword("password");

        when(userService.registerUser(anyString(), anyString())).thenReturn(true);

        AuthResponse response = userController.register(request);

        assertTrue(response.getMessage().contains("successfully"), "Registration should succeed");
    }

    @Test
    void testRegisterUserDuplicate() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");
        request.setPassword("password");

        when(userService.registerUser(anyString(), anyString())).thenReturn(false);

        AuthResponse response = userController.register(request);

        assertTrue(response.getMessage().contains("already exists"), "Should detect duplicate username");
    }

    @Test
    void testLoginUserSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("newUser");
        request.setPassword("password");

        when(userService.authenticateUser(anyString(), anyString())).thenReturn(true);

        AuthResponse response = userController.login(request);

        // Login returns a JWT token on success, not "Login successful" message
        assertTrue(response.getMessage().contains("-mtcgToken") || response.getMessage().split("\\.").length == 3,
                "Login should return a JWT token");
    }

    @Test
    void testLoginUserFailure() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknownUser");
        request.setPassword("wrongPassword");

        when(userService.authenticateUser(anyString(), anyString())).thenReturn(false);

        AuthResponse response = userController.login(request);

        assertTrue(response.getMessage().contains("Invalid credentials"), "Login should fail");
    }
}