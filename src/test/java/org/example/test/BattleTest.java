package org.example.test;

import org.example.models.Card;
import org.example.battle.Battle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BattleTest {

    private Battle battle;

    @BeforeEach
    void setUp() {
        List<Card> player1Deck = Arrays.asList(
                new Card(UUID.randomUUID(), "Dragon", 50, "Fire"),
                new Card(UUID.randomUUID(), "Goblin", 30, "Grass")
        );

        List<Card> player2Deck = Arrays.asList(
                new Card(UUID.randomUUID(), "Knight", 40, "Neutral"),
                new Card(UUID.randomUUID(), "Elf", 20, "Water")
        );

        battle = new Battle("Player1", player1Deck, "Player2", player2Deck); // Provide required parameters
    }

    // Test 1: Basic Monster vs Monster Battle
    @Test
    void testMonsterVsMonster() {
        Card card1 = new Card(UUID.randomUUID(), "Dragon", 50, "Fire");
        Card card2 = new Card(UUID.randomUUID(), "Goblin", 30, "Grass");

        String result = battle.fight(card1, card2);

        assertEquals("Player 1 Wins", result); // Dragon has higher damage.
    }

    // Test 2: Elemental Advantage
    @Test
    void testElementalAdvantage() {
        Card fireCard = new Card(UUID.randomUUID(), "FireMonster", 40, "Fire");
        Card grassCard = new Card(UUID.randomUUID(), "GrassMonster", 40, "Grass");

        String result = battle.fight(fireCard, grassCard);

        assertEquals("Player 1 Wins", result); // Fire > Grass due to elemental advantage.
    }

    // Test 3: Spell vs Monster
    @Test
    void testSpellVsMonster() {
        Card spell = new Card(UUID.randomUUID(), "FireSpell", 45, "Fire");
        Card monster = new Card(UUID.randomUUID(), "WaterMonster", 40, "Water");

        String result = battle.fight(spell, monster);

        assertEquals("Player 1 Wins", result); // Spell beats Monster with higher damage.
    }

    // Test 4: Special Rule (Goblin vs Dragon)
    @Test
    void testSpecialRule_GoblinVsDragon() {
        Card dragon = new Card(UUID.randomUUID(), "Dragon", 40, "Fire");
        Card goblin = new Card(UUID.randomUUID(), "Goblin", 60, "Grass");

        String result = battle.fight(goblin, dragon);

        assertEquals("Player 2 Wins", result); // Goblin fears Dragon regardless of damage.
    }

    // Test 5: Tied Match
    @Test
    void testTiedMatch() {
        Card card1 = new Card(UUID.randomUUID(), "Knight", 40, "Neutral");
        Card card2 = new Card(UUID.randomUUID(), "Warrior", 40, "Neutral");

        String result = battle.fight(card1, card2);

        assertEquals("Tie", result); // Equal damage results in a tie.
    }

    // Test 6: Invalid Input (Null Card)
    @Test
    void testInvalidInput_NullCard() {
        Card validCard = new Card(UUID.randomUUID(), "Knight", 40, "Neutral");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            battle.fight(validCard, null);
        });

        assertEquals("Invalid card input", exception.getMessage());
    }

    // Test 7: Empty Deck Battle
    @Test
    void testEmptyDeckBattle() {
        List<Card> player1Deck = Arrays.asList();
        List<Card> player2Deck = Arrays.asList();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            battle.startBattle();
        });

        assertEquals("Decks cannot be empty", exception.getMessage());
    }

    // Test 8: Multiple Rounds
    @Test
    void testMultipleRounds() {
        // Arrange: Setup decks for both players
        List<Card> player1Deck = Arrays.asList(
                new Card(UUID.randomUUID(), "Dragon", 50, "Fire"),
                new Card(UUID.randomUUID(), "Goblin", 30, "Grass")
        );
        List<Card> player2Deck = Arrays.asList(
                new Card(UUID.randomUUID(), "Knight", 40, "Neutral"),
                new Card(UUID.randomUUID(), "Elf", 20, "Water")
        );

        // Act: Initialize Battle and start the fight
        Battle battle = new Battle("Player1", player1Deck, "Player2", player2Deck);
        String result = battle.startBattle(); // Correctly retrieve the result

        // Assert: Verify outcome
        assertEquals("Player 1 Wins", result); // Expected outcome
    }

    // Test 9: Mixed Results (Win, Lose, Tie)
    @Test
    void testMixedResults() {
        // Arrange: Setup player decks
        List<Card> player1Deck = Arrays.asList(
                new Card(UUID.randomUUID(), "Dragon", 50, "Fire"),
                new Card(UUID.randomUUID(), "Goblin", 20, "Grass")
        );
        List<Card> player2Deck = Arrays.asList(
                new Card(UUID.randomUUID(), "Knight", 50, "Neutral"),
                new Card(UUID.randomUUID(), "Elf", 20, "Water")
        );

        // Act: Initialize Battle and start the fight
        battle = new Battle("Player1", player1Deck, "Player2", player2Deck); // Initialize with constructor arguments
        String result = battle.startBattle(); // Properly call startBattle() and store the result

        // Assert: Verify the outcome
        assertEquals("Tie", result); // Mixed results lead to tie after multiple rounds
    }

    // Test 10: Battle with Invalid Damage Values
    @Test
    void testInvalidDamage() {
        Card invalidCard = new Card(UUID.randomUUID(), "Ghost", -10, "Dark");
        Card validCard = new Card(UUID.randomUUID(), "Elf", 20, "Water");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            battle.fight(invalidCard, validCard);
        });

        assertEquals("Damage value must be positive", exception.getMessage());
    }
}