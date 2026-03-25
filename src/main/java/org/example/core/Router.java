package org.example.core;

import org.example.repositories.*;
import org.example.controllers.*;
import org.example.dtos.*;
import org.example.services.*;
import org.example.models.Card;
import org.example.models.Trade;
import org.example.models.User;
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

    private final UserRepository userRepository = new UserRepository();
    private final UserService userService = new UserService(userRepository);
    private final CardRepository cardRepository = new CardRepository();
    private final UserController userController = new UserController(userService, userRepository, cardRepository);
    private final PackageController packageController = new PackageController();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TransactionController transactionController = new TransactionController();
    private final DeckRepository deckRepository = new DeckRepository();
    private final TradeRepository tradeRepository = new TradeRepository();
    private final TradeService tradeService = new TradeService(tradeRepository, cardRepository, userRepository);
    private final TradeController tradeController = new TradeController(tradeService);
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
            sendJsonResponse(out, response.isSuccess() ? 200 : 401, response);
        }

        // GET /users/{username} - Get user data
        else if (method.equals("GET") && path.startsWith("/users/")) {
            String username = path.substring(7); // Extract username from path
            String authHeader = getHeader(in, "Authorization");
            String requestingUser = validateAndExtractUsername(authHeader);

            if (requestingUser == null) {
                sendJsonResponse(out, 401, Map.of("error", "Unauthorized"));
                return;
            }

            // Only allow if requesting own data or is admin
            if (!requestingUser.equals(username) && !requestingUser.equals("admin")) {
                sendJsonResponse(out, 403, Map.of("error", "Forbidden"));
                return;
            }

            User user = userRepository.getUserByUsername(username);
            if (user == null) {
                sendJsonResponse(out, 404, Map.of("error", "User not found"));
                return;
            }

            // Return user data (Name, Bio, Image)
            Map<String, Object> userData = new HashMap<>();
            userData.put("Name", user.getName());
            userData.put("Bio", user.getBio());
            userData.put("Image", user.getImage());

            sendJsonResponse(out, 200, userData);
        }

        // PUT /users/{username} - Update user data
        else if (method.equals("PUT") && path.startsWith("/users/")) {
            String username = path.substring(7); // Extract username from path
            String authHeader = getHeader(in, "Authorization");
            String requestingUser = validateAndExtractUsername(authHeader);

            if (requestingUser == null) {
                sendJsonResponse(out, 401, Map.of("error", "Unauthorized"));
                return;
            }

            // Only allow if updating own data or is admin
            if (!requestingUser.equals(username) && !requestingUser.equals("admin")) {
                sendJsonResponse(out, 403, Map.of("error", "Forbidden"));
                return;
            }

            User user = userRepository.getUserByUsername(username);
            if (user == null) {
                sendJsonResponse(out, 404, Map.of("error", "User not found"));
                return;
            }

            // Parse request body
            Map<String, String> updateData = objectMapper.readValue(readRequestBody(in), new TypeReference<Map<String, String>>() {});

            // Update user fields
            if (updateData.containsKey("Name")) user.setName(updateData.get("Name"));
            if (updateData.containsKey("Bio")) user.setBio(updateData.get("Bio"));
            if (updateData.containsKey("Image")) user.setImage(updateData.get("Image"));

            boolean updated = userRepository.updateUser(user);
            if (updated) {
                sendJsonResponse(out, 200, Map.of("message", "User successfully updated"));
            } else {
                sendJsonResponse(out, 500, Map.of("error", "Failed to update user"));
            }
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
            UUID userId = userRepository.getUserIdByUsername(username);
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

        //DECK ROUTES ----------------------------------------
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
            UUID userId = userRepository.getUserIdByUsername(username);
            if (userId == null) {
                sendJsonResponse(out, 404, Map.of("error", "User not found"));
                return;
            }

            // Parse request body for card IDs
            List<UUID> cardIds = objectMapper.readValue(readRequestBody(in), new TypeReference<List<UUID>>() {});

            // Validate all cards belong to user and are not in deck/trade
            List<Card> userCards = cardRepository.getCardsByUserId(userId);
            Set<UUID> userCardIds = new HashSet<>();
            for (Card c : userCards) {
                userCardIds.add(c.getId());
            }

            // Check if all provided cards belong to user
            for (UUID cardId : cardIds) {
                if (!userCardIds.contains(cardId)) {
                    sendJsonResponse(out, 403, Map.of("error", "Card does not belong to user or is not available"));
                    return;
                }
            }

            // Validate and set the deck
            boolean success = deckRepository.setDeck(userId, cardIds);
            if (success) {
                // Update card statuses to "deck"
                for (UUID cardId : cardIds) {
                    cardRepository.updateCardStatus(cardId, "deck");
                }
                sendJsonResponse(out, 200, Map.of("message", "Deck updated successfully"));
            } else {
                sendJsonResponse(out, 400, Map.of("error", "Invalid deck setup. Deck must contain exactly 4 cards."));
            }
        }

        else if (method.equals("GET") && path.startsWith("/deck")) { // Retrieve deck
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
                if (username == null) {
                    sendJsonResponse(out, 403, Map.of("error", "Invalid token"));
                    return;
                }
            }

            // Fetch user ID from database
            UUID userId = userRepository.getUserIdByUsername(username);
            if (userId == null) {
                sendJsonResponse(out, 404, Map.of("error", "User not found"));
                return;
            }

            // Check for format query parameter
            String format = "json";
            if (path.contains("?")) {
                String queryString = path.substring(path.indexOf("?") + 1);
                for (String param : queryString.split("&")) {
                    if (param.startsWith("format=")) {
                        format = param.substring(7);
                    }
                }
            }

            // Retrieve deck from repository
            List<Card> deck = deckRepository.getDeck(userId);

            if (deck.isEmpty()) {
                sendJsonResponse(out, 204, Map.of("message", "No cards found in deck"));
            } else {
                if ("plain".equals(format)) {
                    // Return plain text format
                    StringBuilder plainResponse = new StringBuilder();
                    plainResponse.append("Deck for ").append(username).append(":\n");
                    for (int i = 0; i < deck.size(); i++) {
                        Card card = deck.get(i);
                        plainResponse.append(i + 1).append(". ")
                                .append(card.getName())
                                .append(" (Damage: ").append(card.getDamage()).append(")\n");
                    }
                    sendPlainTextResponse(out, 200, plainResponse.toString());
                } else {
                    sendJsonResponse(out, 200, deck);
                }
            }
        }

        // STATS ROUTE ----------------------------------------
        else if (method.equals("GET") && path.equals("/stats")) {
            String authHeader = getHeader(in, "Authorization");
            String username = validateAndExtractUsername(authHeader);

            if (username == null) {
                sendJsonResponse(out, 401, Map.of("error", "Unauthorized"));
                return;
            }

            User user = userRepository.getUserByUsername(username);
            if (user == null) {
                sendJsonResponse(out, 404, Map.of("error", "User not found"));
                return;
            }

            Map<String, Object> stats = new HashMap<>();
            stats.put("Name", user.getName() != null ? user.getName() : user.getUsername());
            stats.put("Elo", user.getElo());
            stats.put("Wins", user.getWins());
            stats.put("Losses", user.getLosses());

            sendJsonResponse(out, 200, stats);
        }

        // SCOREBOARD ROUTE ----------------------------------------
        else if (method.equals("GET") && path.equals("/scoreboard")) {
            String authHeader = getHeader(in, "Authorization");
            String username = validateAndExtractUsername(authHeader);

            if (username == null) {
                sendJsonResponse(out, 401, Map.of("error", "Unauthorized"));
                return;
            }

            // Get all users and sort by ELO descending
            List<User> users = userRepository.findAllUsers();
            users.sort((u1, u2) -> Integer.compare(u2.getElo(), u1.getElo()));

            List<Map<String, Object>> scoreboard = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> stats = new HashMap<>();
                stats.put("Name", user.getName() != null ? user.getName() : user.getUsername());
                stats.put("Elo", user.getElo());
                stats.put("Wins", user.getWins());
                stats.put("Losses", user.getLosses());
                scoreboard.add(stats);
            }

            sendJsonResponse(out, 200, scoreboard);
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

                // Fetch user IDs
                UUID player1Id = userRepository.getUserIdByUsername(player1);
                UUID player2Id = userRepository.getUserIdByUsername(player2);

                // Fetch configured decks for both players (not all cards!)
                List<Card> player1Deck = deckRepository.getDeck(player1Id);
                List<Card> player2Deck = deckRepository.getDeck(player2Id);

                // Validate decks
                if (player1Deck.isEmpty() || player2Deck.isEmpty()) {
                    sendJsonResponse(out, 400, Map.of("error", "Both players need a configured deck to battle."));
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

        //TRADE ROUTES ----------------------------------------
        else if (method.equals("GET") && path.equals("/tradings")) {
            String authHeader = getHeader(in, "Authorization");
            String username = validateAndExtractUsername(authHeader);

            if (username == null) {
                sendJsonResponse(out, 401, Map.of("error", "Unauthorized"));
                return;
            }

            try {
                List<Trade> trades = tradeRepository.getAllTrades();
                if (trades.isEmpty()) {
                    sendJsonResponse(out, 204, trades); // Return empty list with 204
                } else {
                    sendJsonResponse(out, 200, trades); // Return the list of trades
                }
            } catch (Exception e) {
                sendJsonResponse(out, 500, Map.of("error", "Failed to fetch trades"));
            }
        } else if (method.equals("POST") && path.equals("/tradings")) {
            String authHeader = getHeader(in, "Authorization");
            String username = validateAndExtractUsername(authHeader);

            if (username == null) {
                sendJsonResponse(out, 401, Map.of("error", "Unauthorized"));
                return;
            }

            try {
                Trade trade = parseBody(in, Trade.class); // Parse the trade object
                UUID userId = userRepository.getUserIdByUsername(username);

                // Validate the card exists and belongs to user
                Optional<Card> cardOpt = cardRepository.getCardById(trade.getCardToTrade());
                if (cardOpt.isEmpty()) {
                    sendJsonResponse(out, 403, Map.of("error", "Card not found"));
                    return;
                }

                Card card = cardOpt.get();
                if (card.getOwnerId() == null || !card.getOwnerId().equals(userId)) {
                    sendJsonResponse(out, 403, Map.of("error", "Card does not belong to user"));
                    return;
                }

                if ("deck".equals(card.getStatus())) {
                    sendJsonResponse(out, 403, Map.of("error", "Card is locked in deck"));
                    return;
                }

                // Check for duplicate trade ID
                Optional<Trade> existingTrade = tradeRepository.getTradeById(trade.getId());
                if (existingTrade.isPresent()) {
                    sendJsonResponse(out, 409, Map.of("error", "Trade with this ID already exists"));
                    return;
                }

                // Create trade
                boolean success = tradeService.createTrade(trade, userId);
                if (success) {
                    sendJsonResponse(out, 201, Map.of("message", "Trade created successfully"));
                } else {
                    sendJsonResponse(out, 400, Map.of("error", "Failed to create trade"));
                }
            } catch (Exception e) {
                sendJsonResponse(out, 400, Map.of("error", "Failed to create trade: " + e.getMessage()));
            }
        } else if (method.equals("DELETE") && path.startsWith("/tradings/")) {
            String authHeader = getHeader(in, "Authorization");
            String username = validateAndExtractUsername(authHeader);

            if (username == null) {
                sendJsonResponse(out, 401, Map.of("error", "Unauthorized"));
                return;
            }

            try {
                UUID tradeId = UUID.fromString(path.split("/")[2]);
                UUID userId = userRepository.getUserIdByUsername(username);

                // Validate trade exists and belongs to user
                Optional<Trade> tradeOpt = tradeRepository.getTradeById(tradeId);
                if (tradeOpt.isEmpty()) {
                    sendJsonResponse(out, 404, Map.of("error", "Trade not found"));
                    return;
                }

                Trade trade = tradeOpt.get();
                if (!trade.getOwnerId().equals(userId)) {
                    sendJsonResponse(out, 403, Map.of("error", "Trade does not belong to user"));
                    return;
                }

                tradeService.deleteTrade(tradeId, userId);
                sendJsonResponse(out, 200, Map.of("message", "Trade deleted successfully"));
            } catch (Exception e) {
                sendJsonResponse(out, 500, Map.of("error", "Failed to delete trade: " + e.getMessage()));
            }
        } else if (method.equals("POST") && path.startsWith("/tradings/")) {
            String authHeader = getHeader(in, "Authorization");
            String username = validateAndExtractUsername(authHeader);

            if (username == null) {
                sendJsonResponse(out, 401, Map.of("error", "Unauthorized"));
                return;
            }

            try {
                UUID tradeId = UUID.fromString(path.split("/")[2]); // Extract trade ID from path
                String offeredCardIdStr = parseBody(in, String.class); // Offered card ID
                UUID offeredCardId = UUID.fromString(offeredCardIdStr.replace("\"", "").trim());

                UUID buyerId = userRepository.getUserIdByUsername(username);

                // Get trade details
                Optional<Trade> tradeOpt = tradeRepository.getTradeById(tradeId);
                if (tradeOpt.isEmpty()) {
                    sendJsonResponse(out, 404, Map.of("error", "Trade not found"));
                    return;
                }

                Trade trade = tradeOpt.get();

                // Prevent self-trading
                if (trade.getOwnerId().equals(buyerId)) {
                    sendJsonResponse(out, 403, Map.of("error", "Cannot trade with yourself"));
                    return;
                }

                // Validate offered card belongs to buyer
                Optional<Card> offeredCardOpt = cardRepository.getCardById(offeredCardId);
                if (offeredCardOpt.isEmpty()) {
                    sendJsonResponse(out, 403, Map.of("error", "Offered card not found"));
                    return;
                }

                Card offeredCard = offeredCardOpt.get();
                if (offeredCard.getOwnerId() == null || !offeredCard.getOwnerId().equals(buyerId)) {
                    sendJsonResponse(out, 403, Map.of("error", "Offered card does not belong to user"));
                    return;
                }

                if ("deck".equals(offeredCard.getStatus())) {
                    sendJsonResponse(out, 403, Map.of("error", "Offered card is locked in deck"));
                    return;
                }

                // Validate requirements
                if (!offeredCard.getType().equalsIgnoreCase(trade.getType())) {
                    sendJsonResponse(out, 403, Map.of("error", "Offered card type does not match requirements"));
                    return;
                }

                if (offeredCard.getDamage() < trade.getMinimumDamage()) {
                    sendJsonResponse(out, 403, Map.of("error", "Offered card damage does not meet minimum requirement"));
                    return;
                }

                boolean success = tradeService.executeTrade(tradeId, buyerId);
                sendJsonResponse(out, success ? 201 : 400, Map.of("message", success ? "Trade executed successfully" : "Trade execution failed"));
            } catch (Exception e) {
                sendJsonResponse(out, 500, Map.of("error", "Failed to execute trade: " + e.getMessage()));
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

        // Fetch user IDs
        UUID player1Id = userRepository.getUserIdByUsername(player1);
        UUID player2Id = userRepository.getUserIdByUsername(player2);

        // Fetch configured decks for both players
        List<Card> player1Deck = deckRepository.getDeck(player1Id);
        List<Card> player2Deck = deckRepository.getDeck(player2Id);

        // Validate decks
        if (player1Deck.isEmpty() || player2Deck.isEmpty()) {
            sendJsonResponse(out, 400, Map.of("error", "Both players need a configured deck to battle"));
            return;
        }

        // Start the battle
        Battle battle = new Battle(player1, player1Deck, player2, player2Deck);
        String battleResult = battle.startBattle();

        // Send the battle log to both players
        sendJsonResponse(out, 200, Map.of("battleLog", battleResult));
    }

    private String validateAndExtractUsername(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null; // Unauthorized
        }

        String token = authHeader.substring(7); // Remove "Bearer "
        if (token.endsWith("-mtcgToken")) {
            return token.split("-")[0]; // Extract username for custom tokens
        } else {
            return JwtUtil.validateToken(token); // Validate JWT token if applicable
        }
    }


    // Parses the request body into the specified class
    private <T> T parseBody(BufferedReader in, Class<T> clazz) throws Exception {
        String requestBody = readRequestBody(in);
        System.out.println("Raw request body: " + requestBody);
        return objectMapper.readValue(requestBody, clazz);
    }

    // Sends JSON response with appropriate status code
    private void sendJsonResponse(PrintWriter out, int statusCode, Object response) throws Exception {
        String statusText = statusCode == 201 ? " Created" :
                           statusCode == 204 ? " No Content" :
                           statusCode == 401 ? " Unauthorized" :
                           statusCode == 403 ? " Forbidden" :
                           statusCode == 404 ? " Not Found" :
                           statusCode == 409 ? " Conflict" :
                           " OK";
        out.println("HTTP/1.1 " + statusCode + statusText);
        out.println("Content-Type: application/json");
        out.println();
        out.println(objectMapper.writeValueAsString(response));
        out.flush();
    }

    // Sends plain text response
    private void sendPlainTextResponse(PrintWriter out, int statusCode, String response) {
        out.println("HTTP/1.1 " + statusCode + " OK");
        out.println("Content-Type: text/plain");
        out.println();
        out.println(response);
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
