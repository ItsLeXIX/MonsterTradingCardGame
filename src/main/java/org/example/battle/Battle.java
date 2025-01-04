package org.example.battle;

import org.example.models.Card;

import java.util.*;

public class Battle {

    private final List<Card> player1Deck;
    private final List<Card> player2Deck;
    private final List<String> battleLog = new ArrayList<>();

    private static final int MAX_ROUNDS = 100; // Prevent endless loops

    public Battle(List<Card> player1Deck, List<Card> player2Deck) {
        this.player1Deck = new ArrayList<>(player1Deck);
        this.player2Deck = new ArrayList<>(player2Deck);
    }

    public String startBattle() {
        int round = 0;

        while (!player1Deck.isEmpty() && !player2Deck.isEmpty() && round < MAX_ROUNDS) {
            round++;
            battleLog.add("Round " + round + ":");

            // Select random cards
            Card player1Card = getRandomCard(player1Deck);
            Card player2Card = getRandomCard(player2Deck);

            // Log card details
            battleLog.add("Player 1 plays " + player1Card.getName() + " with " + player1Card.getDamage() + " damage.");
            battleLog.add("Player 2 plays " + player2Card.getName() + " with " + player2Card.getDamage() + " damage.");

            // Process the round
            processRound(player1Card, player2Card);
        }

        // Determine the winner
        String result = getBattleResult();
        battleLog.add(result);
        return String.join("\n", battleLog);
    }

    private Card getRandomCard(List<Card> deck) {
        return deck.get(new Random().nextInt(deck.size()));
    }

    private void processRound(Card card1, Card card2) {
        double damage1 = calculateDamage(card1, card2);
        double damage2 = calculateDamage(card2, card1);

        battleLog.add("Damage calculated: Player 1 -> " + damage1 + ", Player 2 -> " + damage2);

        if (damage1 > damage2) {
            battleLog.add("Player 1 wins the round.");
            player1Deck.add(card2); // Take opponent's card
            player2Deck.remove(card2); // Remove from opponent's deck
        } else if (damage2 > damage1) {
            battleLog.add("Player 2 wins the round.");
            player2Deck.add(card1); // Take opponent's card
            player1Deck.remove(card1); // Remove from opponent's deck
        } else {
            battleLog.add("Round ends in a draw.");
        }
    }

    private double calculateDamage(Card attacker, Card defender) {
        // Specialties
        if (isSpecialCase(attacker, defender)) {
            battleLog.add(attacker.getName() + " cannot damage " + defender.getName() + ".");
            return 0;
        }

        // Calculate elemental effect
        double damage = attacker.getDamage();
        if (attacker.getType().equals("spell") || defender.getType().equals("spell")) {
            damage *= getElementMultiplier(attacker.getElement(), defender.getElement());
        }

        return damage;
    }

    private boolean isSpecialCase(Card attacker, Card defender) {
        // Specialty rules
        return (attacker.getName().contains("Goblin") && defender.getName().contains("Dragon")) ||
                (attacker.getName().contains("Wizzard") && defender.getName().contains("Ork")) ||
                (attacker.getName().contains("Knight") && defender.getName().contains("WaterSpell")) ||
                (attacker.getName().contains("Kraken") && defender.getType().equals("spell")) ||
                (attacker.getName().contains("FireElf") && defender.getName().contains("Dragon"));
    }

    private double getElementMultiplier(String attackerElement, String defenderElement) {
        // Element effectiveness
        if (attackerElement.equals("water") && defenderElement.equals("fire")) return 2.0;
        if (attackerElement.equals("fire") && defenderElement.equals("normal")) return 2.0;
        if (attackerElement.equals("normal") && defenderElement.equals("water")) return 2.0;

        if (attackerElement.equals("fire") && defenderElement.equals("water")) return 0.5;
        if (attackerElement.equals("normal") && defenderElement.equals("fire")) return 0.5;
        if (attackerElement.equals("water") && defenderElement.equals("normal")) return 0.5;

        return 1.0; // No effect
    }

    private String getBattleResult() {
        if (player1Deck.isEmpty() && player2Deck.isEmpty()) {
            return "The battle ended in a draw!";
        } else if (player1Deck.isEmpty()) {
            return "Player 2 wins the battle!";
        } else {
            return "Player 1 wins the battle!";
        }
    }
}