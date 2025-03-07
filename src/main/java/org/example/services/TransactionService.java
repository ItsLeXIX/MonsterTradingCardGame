package org.example.services;

import org.example.models.Package;
import org.example.repositories.PackageRepository;
import org.example.repositories.UserRepository;
import org.example.models.User;

public class TransactionService {

    private final PackageRepository packageRepository;
    private final UserRepository userRepository;

    public TransactionService() {
        this.packageRepository = new PackageRepository();
        this.userRepository = new UserRepository();
    }

    public boolean acquirePackage(String username) {
        System.out.println("=== DEBUG: Starting package acquisition ===");
        System.out.println("User: " + username);

        // Fetch user details
        User user = userRepository.getUserByUsername(username);
        if (user == null) {
            System.out.println("ERROR: User not found.");
            return false;
        }
        System.out.println("User Coins: " + user.getCoins());

        // Check if user has enough coins
        if (user.getCoins() < 5) {
            System.out.println("ERROR: Not enough coins.");
            return false;
        }

        // Find an available package
        Package availablePackage = packageRepository.getAvailablePackage();
        if (availablePackage == null) {
            System.out.println("ERROR: No available package found.");
            return false;
        }
        System.out.println("Found package with ID: " + availablePackage.getId());

        // Deduct coins and update DB
        boolean success = packageRepository.purchasePackage(user.getId(), availablePackage.getId());
        if (!success) {
            System.out.println("ERROR: Purchase transaction failed.");
            return false;
        }

        System.out.println("=== SUCCESS: Package acquired! ===");
        return true;
    }
}