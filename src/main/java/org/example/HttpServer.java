package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer {
    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean running = true;
    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());

    public HttpServer(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(10);
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        logger.info("Server started on port " + port);
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("Accepted new connection");
                threadPool.submit(() -> handleClient(clientSocket));
            } catch (IOException e) {
                if (running) {
                    logger.log(Level.SEVERE, "Error accepting connection", e);
                } else {
                    logger.info("Server has been shut down.");
                }
            }
        }
    }

    public void stop() throws IOException {
        logger.info("Shutting down the server...");
        running = false;
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warning("Forcing shutdown...");
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error shutting down the server", e);
            threadPool.shutdownNow();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    private void sendErrorResponse(PrintWriter out, int statusCode, String message) {
        out.println("HTTP/1.1 " + statusCode + " " + getHttpStatusMessage(statusCode));
        out.println("Content-Type: application/json");
        out.println();
        out.println("{\"error\": \"" + message + "\"}");
        out.flush();
    }

    private String getHttpStatusMessage(int statusCode) {
        switch (statusCode) {
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown Error";
        }
    }

    private String readRequestBody(BufferedReader in) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        int contentLength = 0;
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }
        logger.info("Content-Length: " + contentLength);
        char[] buffer = new char[contentLength];
        int totalRead = 0, charsRead;
        while (totalRead < contentLength && (charsRead = in.read(buffer, totalRead, contentLength - totalRead)) != -1) {
            totalRead += charsRead;
        }
        requestBody.append(buffer);
        logger.info("Received Request Body: " + requestBody);
        return requestBody.toString();
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String requestLine = in.readLine();

            if (requestLine != null && !requestLine.isEmpty()) {
                logger.info("Request: " + requestLine);
                String[] parts = requestLine.split(" ");
                String method = parts[0];
                String path = parts[1];

                if (method.equals("POST") && path.equals("/register")) {
                    handlePostRequest(in, out);
                } else if (method.equals("POST") && path.equals("/login")) {
                    handleLoginRequest(in, out);
                } else if (method.equals("GET") && path.equals("/profile")) {
                    String authHeader = null;
                    String line;
                    while (!(line = in.readLine()).isEmpty()) {
                        if (line.startsWith("Authorization:")) {
                            authHeader = line.split(":")[1].trim();
                        }
                    }
                    handleProfileRequest(in, out, authHeader);
                } else {
                    sendErrorResponse(out, 404, "Path not found.");
                }
                out.flush();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error handling client", e);
        }
    }

    private void handlePostRequest(BufferedReader in, PrintWriter out) throws IOException {
        String requestBody = readRequestBody(in);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, String> userData = mapper.readValue(requestBody, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            String username = userData.get("Username");
            String password = userData.get("Password");

            if (username != null && password != null) {
                boolean registered = UserStore.registerUser(username, password);
                if (registered) {
                    logger.info("User successfully registered: " + username);
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: application/json");
                    out.println();
                    out.println("{\"message\": \"User registered successfully.\"}");
                } else {
                    logger.warning("Username already exists: " + username);
                    sendErrorResponse(out, 400, "Username already exists.");
                }
            } else {
                logger.warning("Invalid input received");
                sendErrorResponse(out, 400, "Invalid input.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing JSON or malformed input: " + requestBody, e);
            sendErrorResponse(out, 400, "Malformed JSON.");
        }
        out.flush();
    }

    private void handleLoginRequest(BufferedReader in, PrintWriter out) throws IOException {
        String requestBody = readRequestBody(in);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, String> loginData = mapper.readValue(requestBody, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            String username = loginData.get("Username");
            String password = loginData.get("Password");

            if (UserStore.authenticateUser(username, password)) {
                logger.info("User successfully logged in: " + username);
                String token = JwtUtil.generateToken(username);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println();
                out.println("{\"token\": \"" + token + "\"}");
            } else {
                logger.warning("Invalid credentials for user: " + username);
                sendErrorResponse(out, 401, "Invalid credentials.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing JSON or malformed input: " + requestBody, e);
            sendErrorResponse(out, 400, "Malformed JSON.");
        }
        out.flush();
    }

    private void handleProfileRequest(BufferedReader in, PrintWriter out, String authHeader) throws IOException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendErrorResponse(out, 401, "Missing or invalid token.");
            return;
        }

        String token = authHeader.substring(7);
        String username = JwtUtil.validateToken(token);
        if (username == null) {
            sendErrorResponse(out, 401, "Invalid or expired token.");
            return;
        }

        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println();
        out.println("{\"message\": \"Hello, " + username + "! This is your profile.\"}");
        out.flush();
    }

    public static void main(String[] args) {
        HttpServer server = new HttpServer(8080);
        try {
            server.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start the server", e);
        }
    }
}