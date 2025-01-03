package org.example.models;

import java.util.UUID;

public class MonsterCard extends Card {

    // Constructor for MonsterCard
    public MonsterCard(UUID id, String name, double damage, String element, String status) {
        // Call the parent constructor in Card
        super(id, name, damage, element, "Monster", status); // Fixed argument count
    }
}