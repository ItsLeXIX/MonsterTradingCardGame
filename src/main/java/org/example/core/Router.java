package org.example.core;

import org.example.controllers.UserController;
import org.example.dtos.LoginRequest;
import org.example.dtos.RegisterRequest;
import org.example.dtos.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class Router {

    private final UserController userController = new UserController();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Handle incoming requests
    public void handleRequest(String method, String path, BufferedReader in, PrintWriter out) throws Exception {
        if (method.equals("POST") && path.equals("/users")) { // Fix for registration
            RegisterRequest request = parseBody(in, RegisterRequest.class);
            AuthResponse response = userController.register(request);
            sendJsonResponse(out, response.getMessage().contains("successfully") ? 201 : 409, response);
        } else if (method.equals("POST") && path.equals("/sessions")) { // Fix for login
            LoginRequest request = parseBody(in, LoginRequest.class);
            AuthResponse response = userController.login(request);
            sendJsonResponse(out, response.getMessage().contains("Token") ? 200 : 401, response);
        } else {
            sendJsonResponse(out, 404, Map.of("error", "Path not found"));
        }
    }

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
        String requestBody = readRequestBody(in); // Use the fixed method
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
}