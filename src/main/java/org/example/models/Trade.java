package org.example.models;

import java.util.UUID;

public class Trade {

    private UUID id;
    private UUID cardId;
    private String requiredType;
    private double requiredMinDamage;
    private UUID ownerId;

    // Default Constructor (Required for Jackson)
    public Trade() {
    }

    // Parameterized Constructor
    public Trade(UUID id, UUID cardId, String requiredType, double requiredMinDamage, UUID ownerId) {
        this.id = id;
        this.cardId = cardId;
        this.requiredType = requiredType;
        this.requiredMinDamage = requiredMinDamage;
        this.ownerId = ownerId;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getCardId() { return cardId; }
    public String getRequiredType() { return requiredType; }
    public double getRequiredMinDamage() { return requiredMinDamage; }
    public UUID getOwnerId() { return ownerId; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setCardId(UUID cardId) { this.cardId = cardId; }
    public void setRequiredType(String requiredType) { this.requiredType = requiredType; }
    public void setRequiredMinDamage(double requiredMinDamage) { this.requiredMinDamage = requiredMinDamage; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    // Optional: Override toString for debugging
    @Override
    public String toString() {
        return "Trade{" +
                "id=" + id +
                ", cardId=" + cardId +
                ", requiredType='" + requiredType + '\'' +
                ", requiredMinDamage=" + requiredMinDamage +
                ", ownerId=" + ownerId +
                '}';
    }
}