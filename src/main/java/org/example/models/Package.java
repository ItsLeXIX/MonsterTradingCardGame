package org.example.models;

import java.util.List;

public class Package {
    private int id;
    private List<String> cardIds; // List of UUIDs
    private int price;

    public Package(int id, List<String> cardIds, int price) {
        this.id = id;
        this.cardIds = cardIds;
        this.price = price;
    }

    // Getters
    public int getId() { return id; }
    public List<String> getCardIds() { return cardIds; }
    public int getPrice() { return price; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setCardIds(List<String> cardIds) { this.cardIds = cardIds; }
    public void setPrice(int price) { this.price = price; }
}