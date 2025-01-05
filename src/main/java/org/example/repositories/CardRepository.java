package org.example.repositories;

import org.example.models.Card;
import org.example.util.DatabaseUtil;
import org.example.repositories.UserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CardRepository {

    private final UserRepository userRepository;

    public CardRepository() {
        this.userRepository = new UserRepository();
    }

    // Add a new card
    public boolean addCard(Card card) {
        String sql = "INSERT INTO cards (id, name, damage, type, element, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, card.getId());
            stmt.setString(2, card.getName());
            stmt.setDouble(3, card.getDamage());
            stmt.setString(4, card.getType());
            stmt.setString(5, card.getElement());
            stmt.setString(6, card.getStatus()); // Add status field

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Retrieve all cards
    public List<Card> getAllCards() {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Card card = new Card(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getDouble("damage"),
                        rs.getString("type"),
                        rs.getString("element"),
                        rs.getString("status") // Retrieve status
                );
                cards.add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cards;
    }

    // Get all cards owned by a specific username
    public List<Card> getCardsByUsername(String username) {
        List<Card> cards = new ArrayList<>();

        // Fetch user ID using UserRepository
        Integer userId = userRepository.getUserIdByUsername(username);
        if (userId == null) {
            return cards; // Return empty list if user not found
        }

        // Query to get cards for the user
        String query = "SELECT id, name, damage, type, element, status FROM cards WHERE owner_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                cards.add(new Card(
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
        return cards;
    }

    public Optional<Card> getCardById(UUID id) throws SQLException {
        String query = "SELECT * FROM cards WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Card(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getDouble("damage"),
                        rs.getString("type")  // Assuming 'type' exists in DB
                ));
            }
        }
        return Optional.empty();
    }
}