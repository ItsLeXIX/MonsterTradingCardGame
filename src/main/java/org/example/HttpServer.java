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

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestLine = in.readLine();
            if (requestLine != null && !requestLine.isEmpty()) {
                logger.info("Request: " + requestLine);
                String[] parts = requestLine.split(" ");
                String method = parts[0];
                String path = parts[1];

                if (method.equals("GET") && path.equals("/")) {
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/plain");
                    out.println();
                    out.println("Welcome to the MTCG server!");
                } else if (method.equals("POST") && path.equals("/register")) {
                    logger.info("Handling POST request to /register");
                    handlePostRequest(in, out);
                } else {
                    logger.info("Unknown path: " + path);
                    out.println("HTTP/1.1 404 Not Found");
                    out.println("Content-Type: text/plain");
                    out.println();
                    out.println("Error: Path not found.");
                }
                out.flush();
            } else {
                logger.warning("Empty request line");
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error handling client connection", e);
        }
    }

    private void handlePostRequest(BufferedReader in, PrintWriter out) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        int contentLength = 0;
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }

        logger.info("Content-Length: " + contentLength);
        char[] bodyChars = new char[contentLength];
        int totalRead = 0, readChars;
        while (totalRead < contentLength && (readChars = in.read(bodyChars, totalRead, contentLength - totalRead)) != -1) {
            totalRead += readChars;
        }

        requestBody.append(bodyChars);
        logger.info("Received Request Body: " + requestBody);
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, String> userData = mapper.readValue(requestBody.toString(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            String username = userData.get("Username");
            String password = userData.get("Password");

            if (username != null && password != null) {
                logger.info("User successfully registered: " + username);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println();
                out.println("{\"message\": \"User registered successfully.\"}");
            } else {
                logger.warning("Invalid input received");
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Content-Type: application/json");
                out.println();
                out.println("{\"error\": \"Invalid input.\"}");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing JSON or malformed input: " + requestBody, e);
            out.println("HTTP/1.1 400 Bad Request");
            out.println("Content-Type: application/json");
            out.println();
            out.println("{\"error\": \"Malformed JSON.\"}");
        }
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