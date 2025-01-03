package org.example;

import org.example.core.HttpServer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        HttpServer server = new HttpServer(10001);

        try {
            server.start();
            logger.info("Server started successfully on port 10001");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start the server", e);
        }

        // Graceful shutdown handling
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("Server shutting down...");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during shutdown", e);
            }
        }));
    }
}