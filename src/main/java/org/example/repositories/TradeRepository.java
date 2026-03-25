package org.example.repositories;

import org.example.models.Trade;
import org.example.util.DatabaseUtil;

import java.sql.*;
import java.util.*;

public class TradeRepository {

    // Create a new trade
    public void addTrade(Trade trade) throws SQLException {
        String query = "INSERT INTO trades (id, card_id, type, minimum_damage, owner_id) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, trade.getId());
            stmt.setObject(2, trade.getCardToTrade());
            stmt.setString(3, trade.getType());
            stmt.setDouble(4, trade.getMinimumDamage());
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
                        rs.getString("type"),
                        rs.getDouble("minimum_damage"),
                        UUID.fromString(rs.getString("owner_id"))
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
                        rs.getString("type"),
                        rs.getDouble("minimum_damage"),
                        UUID.fromString(rs.getString("owner_id"))
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

    // Get trade by card ID
    public Optional<Trade> getTradeByCardId(UUID cardId) throws SQLException {
        String query = "SELECT * FROM trades WHERE card_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, cardId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new Trade(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("card_id")),
                        rs.getString("type"),
                        rs.getDouble("minimum_damage"),
                        UUID.fromString(rs.getString("owner_id"))
                ));
            }
        }
        return Optional.empty();
    }

    // Check if trade exists
    public boolean tradeExists(UUID tradeId) throws SQLException {
        String query = "SELECT COUNT(*) FROM trades WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, tradeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
}
