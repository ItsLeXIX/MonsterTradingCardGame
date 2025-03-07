package org.example.repositories;

import org.example.models.Package;
import org.example.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PackageRepository {

    private static final Logger logger = Logger.getLogger(PackageRepository.class.getName());

    // Add a Package
    public boolean addPackage(Package pack) {
        String sql = "INSERT INTO packages (id, status) VALUES (?, ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                UUID packageId = UUID.randomUUID(); // Generate UUID
                stmt.setObject(1, packageId);
                stmt.setString(2, "available");

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    // Insert cards into package_cards table
                    for (String cardId : pack.getCardIds()) {
                        String linkSql = "INSERT INTO package_cards (package_id, card_id) VALUES (?, ?)";
                        try (PreparedStatement linkStmt = conn.prepareStatement(linkSql)) {
                            linkStmt.setObject(1, packageId);
                            linkStmt.setObject(2, UUID.fromString(cardId));
                            linkStmt.executeUpdate();
                        }
                        logger.info("Linking Card " + cardId + " to Package " + packageId);
                    }
                    conn.commit();
                    return true;
                }
            } catch (SQLException e) {
                conn.rollback();
                logger.log(Level.SEVERE, "Error adding package, rolling back transaction", e);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection error", e);
        }
        return false;
    }

    // Retrieve All Packages
    public List<Package> getAllPackages() {
        List<Package> packages = new ArrayList<>();
        String sql = "SELECT p.id, array_agg(pc.card_id) as card_ids FROM packages p " +
                     "JOIN package_cards pc ON p.id = pc.package_id GROUP BY p.id";

        // array_agg(pc.card_id) to group card IDs under each package.
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
                        rs.getObject("id", UUID.class),
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

    public boolean assignPackageToUser(int userId) {
        String query =
                "WITH selected_package AS (" +
                        "    SELECT id FROM packages " +
                        "    WHERE status = 'available' " +  // Check package availability
                        "    LIMIT 1 FOR UPDATE SKIP LOCKED" + // Lock row to prevent double assignment
                        ")" +
                        "UPDATE cards " +
                        "SET owner_id = ? " +  // Assign user ID
                        "WHERE id IN (" +
                        "    SELECT card_id FROM package_cards " +
                        "    WHERE package_id = (SELECT id FROM selected_package)" +
                        ");";

        String deleteQuery =
                "DELETE FROM packages " +
                        "WHERE id IN (SELECT id FROM packages WHERE status = 'available' LIMIT 1);";

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {

                // Assign cards to user
                stmt.setInt(1, userId);
                int updated = stmt.executeUpdate();

                if (updated > 0) {
                    // Delete the assigned package
                    deleteStmt.executeUpdate();

                    // Commit transaction
                    conn.commit();
                    System.out.println("Package assigned successfully.");
                    return true;
                } else {
                    conn.rollback(); // Rollback if assignment fails
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Return false if assignment failed
    }

    public Package getAvailablePackage() {
        String sql = "SELECT id FROM packages WHERE status = 'available' LIMIT 1";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                UUID packageId = rs.getObject("id", UUID.class);
                System.out.println("DEBUG: Found available package: " + packageId);
                return new Package(packageId); // Make sure this constructor exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("DEBUG: No available package found.");
        return null;
    }

    public void updatePackage(Package p) {
        String sql = "UPDATE packages SET status = ?, buyer = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getStatus());
            stmt.setObject(2, p.getBuyer());
            stmt.setObject(3, p.getId());

            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean purchasePackage(UUID buyerId, UUID packageId) {
        logger.info("Attempting to purchase package. Buyer ID: " + buyerId + ", Package ID: " + packageId);

        String updatePackageSQL = "UPDATE packages SET status = 'sold', buyer = ? WHERE id = ?";
        String updateUserCoinsSQL = "UPDATE users SET coins = coins - 5 WHERE id = ?";
        String updateCardOwnershipSQL = "UPDATE cards SET owner_id = ? WHERE id IN (SELECT card_id FROM package_cards WHERE package_id = ?)";

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement stmt = conn.prepareStatement(updatePackageSQL)) {
                stmt.setObject(1, buyerId);
                stmt.setObject(2, packageId);
                int updatedRows = stmt.executeUpdate();
                logger.info("Package update result: " + updatedRows);
            }

            try (PreparedStatement stmt = conn.prepareStatement(updateUserCoinsSQL)) {
                stmt.setObject(1, buyerId);
                int updatedRows = stmt.executeUpdate();
                logger.info("User coins update result: " + updatedRows);
            }

            try (PreparedStatement stmt = conn.prepareStatement(updateCardOwnershipSQL)) {
                stmt.setObject(1, buyerId);
                stmt.setObject(2, packageId);
                int updatedRows = stmt.executeUpdate();
                logger.info("Card ownership update result: " + updatedRows);
            }

            conn.commit(); // Commit transaction
            logger.info("Package purchase committed successfully.");
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error in package purchase", e);
            return false;
        }
    }
}