package org.example.battle;

import org.example.models.Card;

import java.util.List;
import java.util.UUID;

public class BattleTest {

    public static void main(String[] args) {
        List<Card> player1Deck = List.of(
                new Card(UUID.randomUUID(), "Goblin", 10.0, "monster", "fire", "inventory"),
                new Card(UUID.randomUUID(), "WaterSpell", 25.0, "spell", "water", "inventory")
        );

        List<Card> player2Deck = List.of(
                new Card(UUID.randomUUID(), "Dragon", 50.0, "monster", "fire", "inventory"),
                new Card(UUID.randomUUID(), "Knight", 20.0, "monster", "normal", "inventory")
        );

        Battle battle = new Battle(player1Deck, player2Deck);
        String result = battle.startBattle();

        System.out.println(result);
    }
}