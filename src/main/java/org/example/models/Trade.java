package org.example.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class Trade {

    @JsonProperty("Id")
    private UUID id;

    @JsonProperty("CardToTrade")
    private UUID cardToTrade;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("MinimumDamage")
    private double minimumDamage;

    private UUID ownerId;

    // Default Constructor (Required for Jackson)
    public Trade() {
    }

    // Parameterized Constructor
    public Trade(UUID id, UUID cardToTrade, String type, double minimumDamage, UUID ownerId) {
        this.id = id;
        this.cardToTrade = cardToTrade;
        this.type = type;
        this.minimumDamage = minimumDamage;
        this.ownerId = ownerId;
    }

    // Getters
    @JsonProperty("Id")
    public UUID getId() { return id; }

    @JsonProperty("CardToTrade")
    public UUID getCardToTrade() { return cardToTrade; }

    @JsonProperty("Type")
    public String getType() { return type; }

    @JsonProperty("MinimumDamage")
    public double getMinimumDamage() { return minimumDamage; }

    public UUID getOwnerId() { return ownerId; }

    // Setters
    public void setId(UUID id) { this.id = id; }

    @JsonProperty("CardToTrade")
    public void setCardToTrade(UUID cardToTrade) { this.cardToTrade = cardToTrade; }

    @JsonProperty("Type")
    public void setType(String type) { this.type = type; }

    @JsonProperty("MinimumDamage")
    public void setMinimumDamage(double minimumDamage) { this.minimumDamage = minimumDamage; }

    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    // Optional: Override toString for debugging
    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", cardToTrade=" + cardToTrade +
                ", type='" + type + '\'' +
                ", minimumDamage=" + minimumDamage +
                ", ownerId=" + ownerId +
                '}';
    }
}
