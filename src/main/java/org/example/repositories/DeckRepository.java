package org.example.repositories;

import org.example.util.DatabaseUtil;
import org.example.models.Card;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeckRepository {

    // Fetch cards in a user's deck
    public List<Card> getDeckByUserId(int userId) {
        List<Card> deck = new ArrayList<>();
        String query = "SELECT c.id, c.name, c.damage, c.type, c.element, c.status " +
                "FROM decks d JOIN cards c ON d.card_id = c.id " +
                "WHERE d.user_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                deck.add(new Card(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getDouble("damage"),
                        rs.getString("type"),
                        rs.getString("element"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deck;
    }

    // Add cards to the deck
    public boolean setDeck(int userId, List<UUID> cardIds) {
        if (cardIds.size() != 4) { // Deck must have exactly 4 cards
            return false;
        }

        String deleteQuery = "DELETE FROM decks WHERE user_id = ?";
        String insertQuery = "INSERT INTO decks (user_id, card_id) VALUES (?, ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);

            // Clear existing deck
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, userId);
                deleteStmt.executeUpdate();
            }

            // Add new cards
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                for (UUID cardId : cardIds) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setObject(2, cardId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
