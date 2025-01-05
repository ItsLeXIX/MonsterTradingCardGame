package org.example.controllers;

import org.example.models.User;
import org.example.services.UserService;
import org.example.dtos.LoginRequest;
import org.example.dtos.RegisterRequest;
import org.example.dtos.AuthResponse;
import org.example.util.JwtUtil;
import org.example.repositories.CardRepository;
import org.example.repositories.UserRepository;
import org.example.models.Card;

import java.io.PrintWriter;
import java.util.List;

public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;

    public UserController(UserService userService, UserRepository userRepository, CardRepository cardRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
    }

    // Register user endpoint
    public AuthResponse register(RegisterRequest request) {
        boolean registered = userService.registerUser(request.getUsername(), request.getPassword());

        if (registered) {
            return new AuthResponse("User registered successfully.", true); // Success
        }
        return new AuthResponse("Username already exists.", false); // Failure
    }

    // Login user endpoint
    public AuthResponse login(LoginRequest request) {
        boolean isAuthenticated = userService.authenticateUser(request.getUsername(), request.getPassword());

        if (isAuthenticated) {
            String token = JwtUtil.generateToken(request.getUsername());
            return new AuthResponse(token, true); // Success with token
        }
        return new AuthResponse("Invalid credentials.", false); // Failure
    }

    // Fetch all cards for a user based on token
    public void getCards(String token, PrintWriter out) {
        try {
            // Validate token and extract username
            String username = JwtUtil.validateToken(token);
            if (username == null) {
                sendErrorResponse(out, 401, "Unauthorized: Invalid or expired token.");
                return;
            }

            // Fetch cards for the user
            List<Card> cards = cardRepository.getCardsByUsername(username);
            if (cards.isEmpty()) {
                sendErrorResponse(out, 204, "No cards found.");
                return;
            }

            sendJsonResponse(out, 200, cards);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, 500, "Internal server error.");
        }
    }

    // Update user profile
    public void updateProfile(String token, String name, String bio, String image, PrintWriter out) {
        try {
            String username = JwtUtil.validateToken(token);
            if (username == null) {
                sendErrorResponse(out, 401, "Unauthorized: Invalid or expired token.");
                return;
            }

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                sendErrorResponse(out, 404, "User not found.");
                return;
            }

            user.setName(name);
            user.setBio(bio);
            user.setImage(image);

            boolean updated = userRepository.updateUser(user);
            if (updated) {
                sendJsonResponse(out, 200, "Profile updated successfully.");
            } else {
                sendErrorResponse(out, 500, "Failed to update profile.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, 500, "Internal server error.");
        }
    }

    // Utility to send JSON response
    private void sendJsonResponse(PrintWriter out, int statusCode, Object data) throws Exception {
        out.println("HTTP/1.1 " + statusCode + " OK");
        out.println("Content-Type: application/json");
        out.println();
        out.println(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data));
    }

    // Utility to send error response
    private void sendErrorResponse(PrintWriter out, int statusCode, String message) {
        out.println("HTTP/1.1 " + statusCode + " Error");
        out.println("Content-Type: application/json");
        out.println();
        out.println("{\"message\": \"" + message + "\"}");
    }
}