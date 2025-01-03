package org.example.services;

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
        try {
            // Get the user
            User user = userRepository.getUserByUsername(username);
            if (user == null) {
                System.out.println("User not found.");
                return false;
            }

            // Check if the user has enough coins
            if (user.getCoins() < 5) { // Assuming a package costs 5 coins
                System.out.println("Not enough money.");
                return false;
            }

            // Deduct coins and assign package
            boolean success = packageRepository.assignPackageToUser(user.getId());
            if (success) {
                user.setCoins(user.getCoins() - 5);
                userRepository.updateUser(user);
                return true;
            }

            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}