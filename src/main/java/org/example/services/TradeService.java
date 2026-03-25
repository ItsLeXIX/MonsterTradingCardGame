package org.example.services;

import org.example.models.Trade;
import org.example.models.Card;
import org.example.repositories.TradeRepository;
import org.example.repositories.CardRepository;
import org.example.repositories.UserRepository;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class TradeService {
    private final TradeRepository tradeRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public TradeService(TradeRepository tradeRepo, CardRepository cardRepo, UserRepository userRepo) {
        this.tradeRepository = tradeRepo;
        this.cardRepository = cardRepo;
        this.userRepository = userRepo;
    }

    // Create a trade with validation
    public boolean createTrade(Trade trade, UUID ownerId) throws SQLException {
        // Check if card exists
        Optional<Card> cardOpt = cardRepository.getCardById(trade.getCardToTrade());
        if (cardOpt.isEmpty()) {
            throw new IllegalArgumentException("Card not found");
        }

        Card card = cardOpt.get();

        // Check if user owns the card
        if (card.getOwnerId() == null || !card.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("User does not own this card");
        }

        // Check if card is locked in deck
        if ("deck".equals(card.getStatus())) {
            throw new IllegalArgumentException("Card is locked in deck");
        }

        // Check if card is already in a trade
        if ("trade".equals(card.getStatus())) {
            throw new IllegalArgumentException("Card is already in a trade");
        }

        // Set the owner
        trade.setOwnerId(ownerId);

        // Update card status to trade
        card.setStatus("trade");
        cardRepository.updateCardStatus(card.getId(), "trade");

        // Save the trade
        tradeRepository.addTrade(trade);
        return true;
    }

    // Delete a trade with ownership validation
    public boolean deleteTrade(UUID tradeId, UUID userId) throws SQLException {
        Optional<Trade> tradeOpt = tradeRepository.getTradeById(tradeId);
        if (tradeOpt.isEmpty()) {
            return false;
        }

        Trade trade = tradeOpt.get();

        // Check if user owns the trade
        if (!trade.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("User does not own this trade");
        }

        // Get the card and reset its status
        Optional<Card> cardOpt = cardRepository.getCardById(trade.getCardToTrade());
        if (cardOpt.isPresent()) {
            Card card = cardOpt.get();
            card.setStatus("inventory");
            cardRepository.updateCardStatus(card.getId(), "inventory");
        }

        tradeRepository.deleteTrade(tradeId);
        return true;
    }

    // Execute a trade with full validation
    public boolean executeTrade(UUID tradeId, UUID buyerId) throws SQLException {
        Optional<Trade> tradeOpt = tradeRepository.getTradeById(tradeId);
        if (tradeOpt.isEmpty()) return false;

        Trade trade = tradeOpt.get();

        // Prevent self-trading
        if (trade.getOwnerId().equals(buyerId)) {
            throw new IllegalArgumentException("Cannot trade with yourself");
        }

        // Get the offered card details
        Card offeredCard = cardRepository.getCardById(trade.getCardToTrade())
                .orElseThrow(() -> new IllegalArgumentException("Card not found!"));

        // Check if card is still available
        if (!"trade".equals(offeredCard.getStatus())) {
            throw new IllegalArgumentException("Card is not available for trade");
        }

        // Find a matching card from buyer's inventory
        Optional<Card> matchingCard = userRepository.getUserCards(buyerId).stream()
                .filter(c -> c.getType().equalsIgnoreCase(trade.getType()))
                .filter(c -> c.getDamage() >= trade.getMinimumDamage())
                .filter(c -> !"deck".equals(c.getStatus()))  // Card must not be in deck
                .filter(c -> !"trade".equals(c.getStatus()))  // Card must not be in another trade
                .findFirst();

        if (matchingCard.isEmpty()) {
            throw new IllegalArgumentException("No matching card found that meets requirements");
        }

        Card buyerCard = matchingCard.get();

        // Perform the trade - swap ownership
        userRepository.transferCard(buyerId, trade.getOwnerId(), buyerCard.getId());
        userRepository.transferCard(trade.getOwnerId(), buyerId, trade.getCardToTrade());

        // Update card statuses
        offeredCard.setStatus("inventory");
        buyerCard.setStatus("inventory");
        cardRepository.updateCardStatus(offeredCard.getId(), "inventory");
        cardRepository.updateCardStatus(buyerCard.getId(), "inventory");

        // Delete the trade
        tradeRepository.deleteTrade(tradeId);
        return true;
    }
}
