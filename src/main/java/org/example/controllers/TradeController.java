package org.example.controllers;

import org.example.models.Trade;
import org.example.services.TradeService;
import org.example.repositories.TradeRepository;
import org.example.dtos.Request;
import org.example.dtos.Response;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class TradeController {
    private final TradeService tradeService;
    private TradeRepository tradeRepository = new TradeRepository();

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
        this.tradeRepository = tradeRepository;
    }

    // Add a new trade
    public void addTrade(Request req, Response res) {
        try {
            Trade trade = req.getBodyAs(Trade.class);
            tradeRepository.addTrade(trade);
            res.status(201);
        } catch (Exception e) {
            res.status(400);
            res.json("Invalid input: " + e.getMessage());
        }
    }

    // View all trades
    public void getAllTrades(Request req, Response res) throws SQLException {
        List<Trade> trades = tradeRepository.getAllTrades();
        res.json(trades);
    }

    // Execute a trade
    public void executeTrade(Request req, Response res) throws SQLException {
        UUID tradeId = UUID.fromString(req.getParam("id"));
        int buyerId = req.getUserId();
        boolean success = tradeService.executeTrade(tradeId, buyerId);
        res.status(success ? 200 : 400); // OK or Bad Request
    }

    // Delete a trade
    public void deleteTrade(Request req, Response res) throws SQLException {
        UUID tradeId = UUID.fromString(req.getParam("id"));
        tradeRepository.deleteTrade(tradeId);
        res.status(204); // No Content
    }
}