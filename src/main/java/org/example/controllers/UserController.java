package org.example.controllers;

import org.example.services.UserService;
import org.example.dtos.LoginRequest;
import org.example.dtos.RegisterRequest;
import org.example.dtos.AuthResponse;
import org.example.util.JwtUtil;

public class UserController {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    // Register user endpoint
    public AuthResponse register(RegisterRequest request) {
        boolean registered = userService.registerUser(request.getUsername(), request.getPassword());

        // Updated to use new AuthResponse constructor
        if (registered) {
            return new AuthResponse("User registered successfully.", true); // Success
        }
        return new AuthResponse("Username already exists.", false); // Failure
    }

    // Login user endpoint
    public AuthResponse login(LoginRequest request) {
        boolean isAuthenticated = userService.authenticateUser(request.getUsername(), request.getPassword());

        // Updated to use new AuthResponse constructor
        if (isAuthenticated) {
            String token = JwtUtil.generateToken(request.getUsername());
            return new AuthResponse(token, true); // Success with token
        }
        return new AuthResponse("Invalid credentials.", false); // Failure
    }
}