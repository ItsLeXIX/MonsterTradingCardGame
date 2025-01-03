package org.example.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class HttpServer {

    private final int port;
    private final ExecutorService threadPool;
    private final Router router;
    private static final Logger logger = Logger.getLogger(HttpServer.class.getName());

    public HttpServer(int port) {
        this.port = port;
        this.router = new Router();
        this.threadPool = Executors.newFixedThreadPool(10);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Accepted new connection");
                threadPool.submit(new RequestHandler(clientSocket, router));
            }
        } catch (IOException e) {
            logger.severe("Server error: " + e.getMessage());
        }
    }
}