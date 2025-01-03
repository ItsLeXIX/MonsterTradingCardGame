package org.example.repositories;

import org.example.models.Package;
import org.example.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PackageRepository {

    // Add a Package
    public boolean addPackage(Package pack) {
        String sql = "INSERT INTO packages DEFAULT VALUES RETURNING id";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int packageId = rs.getInt(1);

                // Insert cards into package_cards table
                for (String cardId : pack.getCardIds()) {
                    String linkSql = "INSERT INTO package_cards (package_id, card_id) VALUES (?, ?)";
                    try (PreparedStatement linkStmt = conn.prepareStatement(linkSql)) {
                        linkStmt.setInt(1, packageId);
                        linkStmt.setObject(2, UUID.fromString(cardId));
                        linkStmt.executeUpdate();
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    // Retrieve All Packages
    public List<Package> getAllPackages() {
        List<Package> packages = new ArrayList<>();
        String sql = "SELECT p.id, array_agg(pc.card_id) as card_ids FROM packages p " +
                     "JOIN package_cards pc ON p.id = pc.package_id GROUP BY p.id";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                List<String> cardIds = new ArrayList<>();
                Array cardArray = rs.getArray("card_ids");
                if (cardArray != null) {
                    for (UUID id : (UUID[]) cardArray.getArray()) {
                        cardIds.add(id.toString());
                    }
                }

                Package pack = new Package(
                        rs.getInt("id"),
                        cardIds,
                        5 // Default price
                );
                packages.add(pack);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return packages;
    }
}