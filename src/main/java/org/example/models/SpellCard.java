package org.example.models;

import java.util.UUID;

public class SpellCard extends Card {

    // Constructor for SpellCard
    public SpellCard(UUID id, String name, double damage, String element, String status) {
        // Call the parent constructor in Card
        super(id, name, damage, element, "Spell", status); // Fixed argument count
    }
}