package org.example;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        HttpServer server = new HttpServer(8080);
        try {
            server.start();
            logger.info("Server started successfully on port 8080");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start the server", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
                logger.info("Server stopped gracefully");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while stopping the server", e);
            }
        }));
    }
}