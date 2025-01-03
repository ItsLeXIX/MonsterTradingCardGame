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

        if (success) {
            return new AuthResponse("Package created successfully.");
        } else {
            return new AuthResponse("Failed to create package.");
        }
    }
}