package org.example.controllers;

import org.example.dtos.AuthResponse;
import org.example.services.TransactionService;

public class TransactionController {

    private final TransactionService transactionService = new TransactionService();

    public AuthResponse acquirePackage(String username) {
        boolean acquired = transactionService.acquirePackage(username); // Process transaction

        if (acquired) {
            return new AuthResponse("Package acquired successfully.", true); // Success
        } else {
            return new AuthResponse("Failed to acquire package. Not enough coins or no packages available.", false); // Failure
        }
    }
}