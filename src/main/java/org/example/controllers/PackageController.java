package org.example.controllers;

import org.example.models.Card;
import org.example.services.PackageService;
import org.example.dtos.AuthResponse;

import java.util.List;

public class PackageController {

    private final PackageService packageService;

    public PackageController() {
        this.packageService = new PackageService();
    }

    // Create package endpoint
    public AuthResponse createPackage(List<Card> cards) {
        boolean success = packageService.addPackage(cards);

        // Updated to use the new AuthResponse constructor with success flag
        if (success) {
            return new AuthResponse("Package created successfully.", true); // Success case
        } else {
            return new AuthResponse("Failed to create package.", false); // Failure case
        }
    }
}