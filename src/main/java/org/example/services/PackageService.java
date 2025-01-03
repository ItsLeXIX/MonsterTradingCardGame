package org.example.services;

import org.example.models.Card;
import org.example.models.Package;
import org.example.repositories.CardRepository;
import org.example.repositories.PackageRepository;

import java.util.List;
import java.util.UUID;

public class PackageService {

    private final PackageRepository packageRepository;
    private final CardRepository cardRepository;

    public PackageService() {
        this.packageRepository = new PackageRepository();
        this.cardRepository = new CardRepository();
    }

    // Add a new package
    public boolean addPackage(List<Card> cards) {
        try {
            // Validate the package size (e.g., 5 cards per package)
            if (cards.size() != 5) {
                System.out.println("Package must contain exactly 5 cards.");
                return false;
            }

            // Generate a new package ID
            Package newPackage = new Package(0, List.of(), 5); // ID will auto-increment

            // Add cards to the database first
            for (Card card : cards) {
                card.setStatus("inventory"); // Ensure status is set
                cardRepository.addCard(card);
            }

            // Create a list of card IDs to associate with the package
            List<String> cardIds = cards.stream()
                    .map(c -> c.getId().toString())
                    .toList();

            // Update the package with the card IDs
            newPackage.setCardIds(cardIds);

            // Store the package in the database
            packageRepository.addPackage(newPackage);

            System.out.println("Package added successfully.");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}