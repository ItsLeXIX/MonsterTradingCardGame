package org.example.repositories;

import org.example.models.Trade;
import org.example.util.DatabaseUtil;

import java.sql.*;
import java.util.*;

public class TradeRepository {

    // Create a new trade
    public void addTrade(Trade trade) throws SQLException {
        String query = "INSERT INTO trades (id, card_id, required_type, required_min_damage, owner_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, trade.getId());
            stmt.setObject(2, trade.getCardId());
            stmt.setString(3, trade.getRequiredType());
            stmt.setDouble(4, trade.getRequiredMinDamage());
            stmt.setObject(5, trade.getOwnerId());
            stmt.executeUpdate();
        }
    }

    // Retrieve all trades
    public List<Trade> getAllTrades() throws SQLException {
        List<Trade> trades = new ArrayList<>();
        String query = "SELECT * FROM trades";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                trades.add(new Trade(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("card_id")),
                        rs.getString("required_type"),
                        rs.getDouble("required_min_damage"),
                        rs.getObject("owner_id", UUID.class)
                ));
            }
        }
        return trades;
    }

    // Get a specific trade by ID
    public Optional<Trade> getTradeById(UUID id) throws SQLException {
        String query = "SELECT * FROM trades WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Trade(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("card_id")),
                        rs.getString("required_type"),
                        rs.getDouble("required_min_damage"),
                        rs.getObject("owner_id", UUID.class)
                ));
            }
        }
        return Optional.empty();
    }

    // Delete a trade
    public void deleteTrade(UUID id) throws SQLException {
        String query = "DELETE FROM trades WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }
}