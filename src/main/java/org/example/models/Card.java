package org.example.models;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {

    private UUID id;
    private String name;
    private double damage;
    private String element;
    private String type;
    private String status;

    // Primary Constructor
    @JsonCreator
    public Card(
            @JsonProperty("Id") UUID id,
            @JsonProperty("Name") String name,
            @JsonProperty("Damage") double damage,
            @JsonProperty("Element") String element,
            @JsonProperty("Type") String type,
            @JsonProperty("Status") String status
    ) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.element = element;
        this.type = type;
        this.status = status;
    }

    // Simplified Constructor for DB operations
    public Card(UUID id, String name, double damage, String type) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.type = type;
        this.element = null;
        this.status = null;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public double getDamage() { return damage; }
    public String getElement() { return (element == null) ? "none" : element; }
    public String getType() { return type; }
    public String getStatus() { return (status == null) ? "available" : status; }

    public void setId(UUID id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDamage(double damage) { this.damage = damage; }
    public void setElement(String element) { this.element = element; }
    public void setType(String type) { this.type = type; }
    public void setStatus(String status) { this.status = status; }

    // Matching criteria for trading
    public boolean matchesCriteria(String requiredType, double minDamage) {
        return this.type.equalsIgnoreCase(requiredType) && this.damage >= minDamage;
    }

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", damage=" + damage +
                ", element='" + element + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}