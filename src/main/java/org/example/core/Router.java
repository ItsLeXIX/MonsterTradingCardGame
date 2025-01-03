package org.example.core;

import org.example.controllers.UserController;
import org.example.controllers.PackageController;
import org.example.dtos.LoginRequest;
import org.example.dtos.RegisterRequest;
import org.example.dtos.AuthResponse;
import org.example.models.Card;
import org.example.controllers.TransactionController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class Router {

    private final UserController userController = new UserController();
    private final PackageController packageController = new PackageController();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TransactionController transactionController = new TransactionController();

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
            String authHeader = getHeader(in, "Authorization"); // Read Authorization header properly
            if (authHeader == null || !authHeader.equals("Bearer admin-mtcgToken")) {
                sendJsonResponse(out, 403, Map.of("error", "Unauthorized"));
                return;
            }

            // Parse request body into a list of cards
            List<Card> cards = objectMapper.readValue(
                    readRequestBody(in), new TypeReference<List<Card>>() {} // FIXED parsing
            );

            // Process package creation
            AuthResponse response = packageController.createPackage(cards);
            sendJsonResponse(out, response.getMessage().contains("successfully") ? 201 : 400, response);
        }

        //TRANSACTION ROUTES ----------------------------------------
        else if (method.equals("POST") && path.equals("/transactions/packages")) { // Acquire package
            String authHeader = null;

            // Read headers to find Authorization
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                if (line.startsWith("Authorization: Bearer ")) {
                    authHeader = line;
                    break;
                }
            }

            // Validate Authorization header
            if (authHeader == null || !authHeader.startsWith("Authorization: Bearer ")) {
                sendJsonResponse(out, 403, Map.of("error", "Unauthorized"));
                return;
            }

            // Extract username from token
            String token = authHeader.substring(22); // "Bearer " length = 7
            if (!token.endsWith("-mtcgToken")) { // Validate token format
                sendJsonResponse(out, 403, Map.of("error", "Invalid token format"));
                return;
            }
            String username = token.split("-")[0];

            // Process transaction
            AuthResponse response = transactionController.acquirePackage(username);

            // Use success flag to set HTTP status codes
            sendJsonResponse(out, response.isSuccess() ? 201 : 403, response); // New response handling
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

    // Parses the request body into the specified class
    private <T> T parseBody(BufferedReader in, Class<T> clazz) throws Exception {
        String requestBody = readRequestBody(in);
        System.out.println("Raw request body: " + requestBody); // Debugging
        return objectMapper.readValue(requestBody, clazz);
    }

    // Sends JSON response with appropriate status code
    private void sendJsonResponse(PrintWriter out, int statusCode, Object response) throws Exception {
        out.println("HTTP/1.1 " + statusCode + (statusCode == 201 ? " Created" : " OK"));
        out.println("Content-Type: application/json");
        out.println();
        out.println(objectMapper.writeValueAsString(response));
        out.flush(); // Flush output to ensure the response is sent immediately
    }

    // Reads specific header value
    private String getHeader(BufferedReader in, String headerName) throws IOException {
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) { // Read headers
            if (line.startsWith(headerName + ":")) { // Match header name
                return line.split(":")[1].trim(); // Extract value
            }
        }
        return null; // Return null if header not found
    }
}