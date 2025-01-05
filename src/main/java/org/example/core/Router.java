package org.example.core;

import com.fasterxml.jackson.core.JacksonException;
import org.example.repositories.*;
import org.example.controllers.*;
import org.example.dtos.*;
import org.example.models.Card;
import org.example.battle.Battle;
import org.example.util.JwtUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Router {

    private final UserController userController = new UserController();
    private final PackageController packageController = new PackageController();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TransactionController transactionController = new TransactionController();
    private final CardRepository cardRepository = new CardRepository();
    private final UserRepository userRepository = new UserRepository();
    private final DeckRepository deckRepository = new DeckRepository();
    private final JwtUtil jwtUtil = new JwtUtil();


    // Queue to store players waiting for a battle
    private static final Queue<String> waitingPlayers = new ConcurrentLinkedQueue<>();

    // Handle incoming requests
    public void handleRequest(String method, String path, BufferedReader in, PrintWriter out) throws Exception {

        // USER ROUTES -------------------------------------------
        if (method.equals("POST") && path.equals("/users")) { // User registration
            RegisterRequest request = parseBody(in, RegisterRequest.class);
            AuthResponse response = userController.register(request);
            sendJsonResponse(out, response.getMessage().contains("successfully") ? 201 : 409, response);
        } else if (method.equals("POST") && path.equals("/sessions")) { // User login
            LoginRequest request = parseBody(in, LoginRequest.class);
            AuthResponse response = userController.login(request);
            sendJsonResponse(out, response.getMessage().contains("Token") ? 200 : 401, response);
        }

        // PACKAGE ROUTES ----------------------------------------
        else if (method.equals("POST") && path.equals("/packages")) { // Add packages
            // Authorization Header Check
            String authHeader = getHeader(in, "Authorization");
            if (authHeader == null || !authHeader.equals("Bearer admin-mtcgToken")) {
                sendJsonResponse(out, 403, Map.of("error", "Unauthorized"));
                return;
            }

            // Parse request body into a list of cards
            List<Card> cards = objectMapper.readValue(
                    readRequestBody(in), new TypeReference<List<Card>>() {}
            );

            // Process package creation
            AuthResponse response = packageController.createPackage(cards);
            sendJsonResponse(out, response.getMessage().contains("successfully") ? 201 : 400, response);
        }

        // TRANSACTION ROUTES ----------------------------------------
        else if (method.equals("POST") && path.equals("/transactions/packages")) { // Acquire package
            String authHeader = getHeader(in, "Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendJsonResponse(out, 403, Map.of("error", "Unauthorized"));
                return;
            }

            // Extract username from token
            String token = authHeader.substring(7);
            if (!token.endsWith("-mtcgToken")) {
                sendJsonResponse(out, 403, Map.of("error", "Invalid token format"));
                return;
            }
            String username = token.split("-")[0];

            // Process transaction
            AuthResponse response = transactionController.acquirePackage(username);
            sendJsonResponse(out, response.isSuccess() ? 201 : 403, response);
        }

        else if (method.equals("GET") && path.equals("/cards")) { // Retrieve user cards
            String authHeader = getHeader(in, "Authorization");

            // Validate Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendJsonResponse(out, 403, Map.of("error", "Unauthorized"));
                return;
            }

            // Extract and validate token
            String token = authHeader.substring(7); // Remove "Bearer "
            String username = null;

            // Handle username-based tokens (e.g., kienboec-mtcgToken)
            if (token.endsWith("-mtcgToken")) { // Username-based token
                username = token.split("-")[0]; // Extract username directly
            }
            // Handle JWT tokens
            else {
                username = JwtUtil.validateToken(token); // Validate JWT and extract username
            }

            // Check if username is valid
            if (username == null) {
                sendJsonResponse(out, 403, Map.of("error", "Invalid token"));
                return;
            }

            // Fetch user ID from database
            Integer userId = userRepository.getUserIdByUsername(username);
            if (userId == null) { // User not found
                sendJsonResponse(out, 404, Map.of("error", "User not found"));
                return;
            }

            // Fetch all cards owned by the user
            List<Card> cards = cardRepository.getCardsByUsername(username);

            if (cards.isEmpty()) {
                sendJsonResponse(out, 204, Map.of("message", "No cards found"));
            } else {
                sendJsonResponse(out, 200, cards);
            }
        }

        //DECK ROUTE ----------------------------------------
        else if (method.equals("PUT") && path.equals("/deck")) { // Set deck
            String authHeader = getHeader(in, "Authorization");

            // Validate Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendJsonResponse(out, 403, Map.of("error", "Unauthorized"));
                return;
            }

            // Extract the token
            String token = authHeader.substring(7); // Remove "Bearer "
            String username = null;

            // Handle username-based tokens
            if (token.endsWith("-mtcgToken")) {
                username = token.split("-")[0]; // Extract username
            }
            // Handle JWT tokens
            else {
                username = JwtUtil.validateToken(token); // Validate and extract username
                if (username == null) { // JWT validation failed
                    sendJsonResponse(out, 403, Map.of("error", "Invalid token"));
                    return;
                }
            }

            // Fetch user ID from database
            Integer userId = userRepository.getUserIdByUsername(username);
            if (userId == null) {
                sendJsonResponse(out, 404, Map.of("error", "User not found"));
                return;
            }

            // Parse request body for card IDs
            List<UUID> cardIds = objectMapper.readValue(readRequestBody(in), new TypeReference<List<UUID>>() {});

            // Validate and set the deck
            boolean success = deckRepository.setDeck(userId, cardIds);
            if (success) {
                sendJsonResponse(out, 200, Map.of("message", "Deck updated successfully"));
            } else {
                sendJsonResponse(out, 400, Map.of("error", "Invalid deck setup. Deck must contain exactly 4 cards."));
            }

            System.out.println("Token: " + token);
            System.out.println("Extracted username: " + username);
            System.out.println("User ID: " + userId);
        }

        else if (method.equals("PUT") && path.equals("/deck")) { // Set deck
            String authHeader = getHeader(in, "Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendJsonResponse(out, 403, Map.of("error", "Unauthorized"));
                return;
            }

            String token = authHeader.substring(7);
            String username = token.split("-")[0];
            Integer userId = userRepository.getUserIdByUsername(username);

            if (userId == null) {
                sendJsonResponse(out, 404, Map.of("error", "User not found"));
                return;
            }

            // Parse request body
            List<UUID> cardIds = objectMapper.readValue(readRequestBody(in), new TypeReference<List<UUID>>() {});
            boolean success = deckRepository.setDeck(userId, cardIds);

            if (success) {
                sendJsonResponse(out, 200, Map.of("message", "Deck updated successfully"));
            } else {
                sendJsonResponse(out, 400, Map.of("error", "Invalid deck setup. Deck must contain exactly 4 cards."));
            }
        }

        //BATTLE ROUTE ----------------------------------------
        else if (method.equals("POST") && path.equals("/battles")) { // Join a battle
            String authHeader = getHeader(in, "Authorization");

            // Validate Authorization header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendJsonResponse(out, 403, Map.of("error", "Unauthorized"));
                return;
            }

            // Extract and validate token
            String token = authHeader.substring(7);
            String username = null;

            // Handle username-based tokens
            if (token.endsWith("-mtcgToken")) {
                username = token.split("-")[0];
            } else {
                username = JwtUtil.validateToken(token);
            }

            // Check if username is valid
            if (username == null) {
                sendJsonResponse(out, 403, Map.of("error", "Invalid token"));
                return;
            }

            // Add player to waiting queue
            waitingPlayers.add(username);

            // Check if we have two players and start the battle immediately
            if (waitingPlayers.size() >= 2) {
                // Get both players from the queue
                String player1 = waitingPlayers.poll();
                String player2 = waitingPlayers.poll();

                // Fetch decks for both players
                List<Card> player1Deck = cardRepository.getCardsByUsername(player1);
                List<Card> player2Deck = cardRepository.getCardsByUsername(player2);

                // Validate decks
                if (player1Deck.isEmpty() || player2Deck.isEmpty()) {
                    sendJsonResponse(out, 400, Map.of("error", "Both players need at least one card to battle."));
                    return;
                }

                // Start the battle
                Battle battle = new Battle(player1, player1Deck, player2, player2Deck);

                // Flush logs incrementally
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println();
                out.flush(); // Flush initial headers

                for (String log : battle.startBattleLogs()) { // New method to stream logs incrementally
                    out.println(log);
                    out.flush(); // Flush after each log line
                }

                // Final flush
                out.flush();
            } else {
                // Send waiting message if fewer than 2 players
                sendJsonResponse(out, 200, Map.of("message", "Player " + username + " is waiting for a battle."));
            }
        }

        // DEFAULT ROUTE ----------------------------------------
        else {
            sendJsonResponse(out, 404, Map.of("error", "Path not found"));
        }
    }

    // --- Utility methods ---

    // Reads body by skipping headers
    private String readRequestBody(BufferedReader in) throws IOException {
        StringBuilder body = new StringBuilder();
        String line;

        // Skip headers
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            // Stop at an empty line (end of headers)
        }

        // Read body content
        while (in.ready()) {
            body.append((char) in.read());
        }

        return body.toString();
    }

    private void startBattle(PrintWriter out) throws Exception {
        // Fetch players from the queue
        String player1 = waitingPlayers.poll();
        String player2 = waitingPlayers.poll();

        // Fetch decks for both players
        List<Card> player1Deck = cardRepository.getCardsByUsername(player1);
        List<Card> player2Deck = cardRepository.getCardsByUsername(player2);

        // Validate decks
        if (player1Deck.isEmpty() || player2Deck.isEmpty()) {
            sendJsonResponse(out, 400, Map.of("error", "Both players need at least one card to battle"));
            return;
        }

        // Start the battle
        Battle battle = new Battle(player1, player1Deck, player2, player2Deck);
        String battleResult = battle.startBattle();

        // Send the battle log to both players
        sendJsonResponse(out, 200, Map.of("battleLog", battleResult));
    }


    // Parses the request body into the specified class
    private <T> T parseBody(BufferedReader in, Class<T> clazz) throws Exception {
        String requestBody = readRequestBody(in);
        System.out.println("Raw request body: " + requestBody);
        return objectMapper.readValue(requestBody, clazz);
    }

    // Sends JSON response with appropriate status code
    private void sendJsonResponse(PrintWriter out, int statusCode, Object response) throws Exception {
        out.println("HTTP/1.1 " + statusCode + (statusCode == 201 ? " Created" : " OK"));
        out.println("Content-Type: application/json");
        out.println();
        out.println(objectMapper.writeValueAsString(response));
        out.flush();
    }

    // Reads specific header value
    private String getHeader(BufferedReader in, String headerName) throws IOException {
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith(headerName + ":")) {
                return line.split(":")[1].trim();
            }
        }
        return null;
    }
}