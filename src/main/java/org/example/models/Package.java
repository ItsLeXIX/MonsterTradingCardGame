package org.example.models;

import java.util.List;
import java.util.UUID;

public class Package {
    private UUID id;
    private List<String> cardIds;
    private int price;
    private String status;
    private UUID buyer;

    public Package(UUID id, List<String> cardIds, int price) {
        this.id = id;
        this.cardIds = cardIds;
        this.price = price;
        this.status = "available";
        this.buyer = null;
    }

    // Overloaded constructor for fetching a package from DB
    public Package(UUID id) {
        this.id = id;
        this.cardIds = null;
        this.buyer = null;
    }

    // Getters
    public UUID getId() { return id; }
    public List<String> getCardIds() { return cardIds; }
    public int getPrice() { return price; }
    public String getStatus() { return status; }
    public UUID getBuyer() { return buyer; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setCardIds(List<String> cardIds) { this.cardIds = cardIds; }
    public void setPrice(int price) { this.price = price; }
    public void setStatus(String status) { this.status = status; }
    public void setBuyer(UUID buyer) { this.buyer = buyer; }
}