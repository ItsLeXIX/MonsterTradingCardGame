package org.example.battle;

import org.example.models.Card;
import org.example.repositories.UserRepository;
import org.example.models.User;

import java.util.*;

public class Battle {

    private final List<Card> player1Deck;
    private final List<Card> player2Deck;
    private static final Map<String, String> ELEMENTAL_ADVANTAGES = new HashMap<>();
    static {
        ELEMENTAL_ADVANTAGES.put("Fire", "Grass");
        ELEMENTAL_ADVANTAGES.put("Grass", "Water");
        ELEMENTAL_ADVANTAGES.put("Water", "Fire");
    }
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

            //debug
            battleLog.add("Player 1 deck size: " + player1Deck.size());
            battleLog.add("Player 2 deck size: " + player2Deck.size());
        }


        // Determine the winner
        String result = getBattleResult();
        battleLog.add(result);

        // Update player stats based on the result
        updatePlayerStats(result);

        return String.join("\n", battleLog);

    }

    public List<String> startBattleLogs() {
        List<String> logs = new ArrayList<>();

        int round = 0;
        while (!player1Deck.isEmpty() && !player2Deck.isEmpty() && round < MAX_ROUNDS) {
            round++;
            logs.add("Round " + round + ":");

            // Select random cards
            Card player1Card = getRandomCard(player1Deck);
            Card player2Card = getRandomCard(player2Deck);

            // Log card details
            logs.add("Player 1 (" + player1Name + ") plays " + player1Card.getName() + " with " + player1Card.getDamage() + " damage.");
            logs.add("Player 2 (" + player2Name + ") plays " + player2Card.getName() + " with " + player2Card.getDamage() + " damage.");

            // Process round
            processRound(player1Card, player2Card);
        }

        // Determine winner
        logs.add(getBattleResult());

        // Update player stats
        updatePlayerStats(getBattleResult());

        return logs; // Return logs incrementally
    }

    private Card getRandomCard(List<Card> deck) {
        return deck.get(new Random().nextInt(deck.size()));
    }

    private void processRound(Card card1, Card card2) {
        // Calculate damage for both players
        double damage1 = calculateDamage(card1, card2);
        double damage2 = calculateDamage(card2, card1);

        // Log calculated damage
        battleLog.add("Damage calculated: Player 1 -> " + damage1 + ", Player 2 -> " + damage2);
        System.out.println("Damage calculated: Player 1 -> " + damage1 + ", Player 2 -> " + damage2);

        if (damage1 > damage2) {
            // Player 1 wins the round
            battleLog.add("Player 1 wins the round.");

            // Safely remove the card from Player 2's deck using equals()
            boolean removed = player2Deck.removeIf(c -> c.getId().equals(card2.getId()));
            if (removed) {
                // Add the **original card** to Player 1's deck (not a cloned card)
                player1Deck.add(card2);
            } else {
                System.err.println("Failed to remove card from Player 2 deck: " + card2.getId());
            }

        } else if (damage2 > damage1) {
            // Player 2 wins the round
            battleLog.add("Player 2 wins the round.");

            // Safely remove the card from Player 1's deck using equals()
            boolean removed = player1Deck.removeIf(c -> c.getId().equals(card1.getId()));
            if (removed) {
                // Add the **original card** to Player 2's deck (not a cloned card)
                player2Deck.add(card1);
            } else {
                System.err.println("Failed to remove card from Player 1 deck: " + card1.getId());
            }

        } else {
            // Round ends in a draw
            battleLog.add("Round ends in a draw.");

            // Fatigue Mechanic: Remove a random card from each deck to avoid infinite loops
            if (!player1Deck.isEmpty()) {
                Card fatigueCard1 = getRandomCard(player1Deck);
                player1Deck.remove(fatigueCard1);
                battleLog.add("Player 1 loses a card: " + fatigueCard1.getName());
            }
            if (!player2Deck.isEmpty()) {
                Card fatigueCard2 = getRandomCard(player2Deck);
                player2Deck.remove(fatigueCard2);
                battleLog.add("Player 2 loses a card: " + fatigueCard2.getName());
            }
        }
    }

    private double calculateDamage(Card attacker, Card defender) {
        if (isSpecialCase(attacker, defender)) {
            battleLog.add(attacker.getName() + " cannot damage " + defender.getName() + ".");
            System.out.println(attacker.getName() + " cannot damage " + defender.getName());
            return 0;
        }

        double damage = attacker.getDamage();
        String attackerType = attacker.getType() != null ? attacker.getType() : "monster";
        String defenderType = defender.getType() != null ? defender.getType() : "monster";
        String attackerElement = attacker.getElement() != null ? attacker.getElement() : "normal";
        String defenderElement = defender.getElement() != null ? defender.getElement() : "normal";

        if (attackerType.equals("spell") || defenderType.equals("spell")) {
            double multiplier = getElementMultiplier(attackerElement, defenderElement);
            damage *= multiplier;
            System.out.println("Elemental multiplier: " + multiplier);
        }

        System.out.println(attacker.getName() + " calculated damage: " + damage);
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

    // Fight function between two cards
    public String fight(Card card1, Card card2) {
        // Input validation
        if (card1 == null || card2 == null) {
            throw new IllegalArgumentException("Invalid card input");
        }
        if (card1.getDamage() < 0 || card2.getDamage() < 0) {
            throw new IllegalArgumentException("Damage value must be positive");
        }

        // Check special rules
        if (isSpecialRule(card1, card2)) {
            return determineSpecialRuleWinner(card1, card2);
        }

        // Check elemental advantage
        double card1Damage = calculateEffectiveDamage(card1, card2);
        double card2Damage = calculateEffectiveDamage(card2, card1);

        // Determine winner
        if (card1Damage > card2Damage) {
            return "Player 1 Wins";
        } else if (card2Damage > card1Damage) {
            return "Player 2 Wins";
        } else {
            return "Tie";
        }
    }

    // Check for special rules between two cards
    private boolean isSpecialRule(Card card1, Card card2) {
        return (card1.getName().contains("Goblin") && card2.getName().contains("Dragon")) ||
                (card1.getName().contains("Kraken") && card2.getName().contains("Spell"));
    }

    // Determine the winner based on special rules
    private String determineSpecialRuleWinner(Card card1, Card card2) {
        if (card1.getName().contains("Goblin") && card2.getName().contains("Dragon")) {
            return "Player 2 Wins"; // Goblins fear Dragons
        }
        if (card1.getName().contains("Kraken") && card2.getName().contains("Spell")) {
            return "Player 1 Wins"; // Kraken is immune to spells
        }
        return "Tie"; // Default fallback
    }

    // Calculate effective damage considering elemental advantages
    private double calculateEffectiveDamage(Card attacker, Card defender) {
        if (ELEMENTAL_ADVANTAGES.getOrDefault(attacker.getElementType(), "").equals(defender.getElementType())) {
            return attacker.getDamage() * 2; // Double damage if element is advantageous
        } else if (ELEMENTAL_ADVANTAGES.getOrDefault(defender.getElementType(), "").equals(attacker.getElementType())) {
            return attacker.getDamage() / 2; // Half damage if element is weak
        }
        return attacker.getDamage(); // Normal damage otherwise
    }
}
