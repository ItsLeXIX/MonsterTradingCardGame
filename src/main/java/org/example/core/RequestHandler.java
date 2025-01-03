package org.example.core;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {

    private final Socket clientSocket;
    private final Router router;
    private static final Logger logger = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket clientSocket, Router router) {
        this.clientSocket = clientSocket;
        this.router = router;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestLine = in.readLine();
            logger.info("Request: " + requestLine);

            if (requestLine != null && !requestLine.isEmpty()) {
                String[] parts = requestLine.split(" ");
                String method = parts[0];
                String path = parts[1];

                router.handleRequest(method, path, in, out);
            } else {
                sendErrorResponse(out, 400, "Bad Request");
            }
        } catch (Exception e) {
            logger.severe("Error handling request: " + e.getMessage());
        }
    }

    private void sendErrorResponse(PrintWriter out, int statusCode, String message) {
        out.println("HTTP/1.1 " + statusCode + " Error");
        out.println("Content-Type: application/json");
        out.println();
        out.println("{\"error\": \"" + message + "\"}");
        out.flush();
    }
}