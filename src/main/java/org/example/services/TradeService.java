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

    // Execute a trade
    public boolean executeTrade(UUID tradeId, int buyerId) throws SQLException {
        Optional<Trade> tradeOpt = tradeRepository.getTradeById(tradeId);
        if (tradeOpt.isEmpty()) return false;

        Trade trade = tradeOpt.get();
        Card offeredCard = cardRepository.getCardById(trade.getCardId())
                .orElseThrow(() -> new IllegalArgumentException("Card not found!"));
        Optional<Card> matchingCard = userRepository.getUserCards(buyerId).stream()
                .filter(c -> c.getType().equals(trade.getRequiredType()) && c.getDamage() >= trade.getRequiredMinDamage())
                .findFirst();

        if (matchingCard.isPresent()) {
            // Perform the trade
            userRepository.transferCard(buyerId, trade.getOwnerId(), matchingCard.get().getId());
            userRepository.transferCard(trade.getOwnerId(), buyerId, trade.getCardId());
            tradeRepository.deleteTrade(tradeId);
            return true;
        }
        return false;
    }
}