package org.example.battle;

import org.example.models.Card;
import org.example.repositories.UserRepository;
import org.example.models.User;

import java.util.*;

public class Battle {

    private final List<Card> player1Deck;
    private final List<Card> player2Deck;
    private final List<String> battleLog = new ArrayList<>();

    private static final int MAX_ROUNDS = 100; // Prevent endless loops

    private final String player1Name;
    private final String player2Name;
    private final UserRepository userRepository = new UserRepository();

    public Battle(String player1Name, List<Card> player1Deck, String player2Name, List<Card> player2Deck) {
        this.player1Name = player1Name;
        this.player1Deck = new ArrayList<>(player1Deck);
        this.player2Name = player2Name;
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
            battleLog.add("Player 1 (" + player1Name + ") plays " + player1Card.getName() + " with " + player1Card.getDamage() + " damage.");
            battleLog.add("Player 2 (" + player2Name + ") plays " + player2Card.getName() + " with " + player2Card.getDamage() + " damage.");

            // Process the round
            processRound(player1Card, player2Card);
        }

        // Determine the winner
        String result = getBattleResult();
        battleLog.add(result);

        // Update player stats based on the result
        updatePlayerStats(result);

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
        if (isSpecialCase(attacker, defender)) {
            battleLog.add(attacker.getName() + " cannot damage " + defender.getName() + ".");
            return 0;
        }

        double damage = attacker.getDamage();
        if (attacker.getType().equals("spell") || defender.getType().equals("spell")) {
            damage *= getElementMultiplier(attacker.getElement(), defender.getElement());
        }

        return damage;
    }

    private boolean isSpecialCase(Card attacker, Card defender) {
        return (attacker.getName().contains("Goblin") && defender.getName().contains("Dragon")) ||
                (attacker.getName().contains("Wizzard") && defender.getName().contains("Ork")) ||
                (attacker.getName().contains("Knight") && defender.getName().contains("WaterSpell")) ||
                (attacker.getName().contains("Kraken") && defender.getType().equals("spell")) ||
                (attacker.getName().contains("FireElf") && defender.getName().contains("Dragon"));
    }

    private double getElementMultiplier(String attackerElement, String defenderElement) {
        if (attackerElement.equals("water") && defenderElement.equals("fire")) return 2.0;
        if (attackerElement.equals("fire") && defenderElement.equals("normal")) return 2.0;
        if (attackerElement.equals("normal") && defenderElement.equals("water")) return 2.0;

        if (attackerElement.equals("fire") && defenderElement.equals("water")) return 0.5;
        if (attackerElement.equals("normal") && defenderElement.equals("fire")) return 0.5;
        if (attackerElement.equals("water") && defenderElement.equals("normal")) return 0.5;

        return 1.0;
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

    private void updatePlayerStats(String result) {
        User player1 = userRepository.getUserByUsername(player1Name);
        User player2 = userRepository.getUserByUsername(player2Name);

        if (result.contains("Player 1")) {
            player1.setWins(player1.getWins() + 1);
            player1.setElo(player1.getElo() + 10);
            player2.setLosses(player2.getLosses() + 1);
            player2.setElo(player2.getElo() - 5);
        } else if (result.contains("Player 2")) {
            player2.setWins(player2.getWins() + 1);
            player2.setElo(player2.getElo() + 10);
            player1.setLosses(player1.getLosses() + 1);
            player1.setElo(player1.getElo() - 5);
        } else {
            // Draw case, no ELO change
            player1.setElo(player1.getElo());
            player2.setElo(player2.getElo());
        }

        // Update in DB
        userRepository.updateUser(player1);
        userRepository.updateUser(player2);
    }
}