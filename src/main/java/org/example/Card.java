package org.example;

public class Card {
    private String name;
    private double damage;
    private String elementType;

    public Card(String name, double damage, String elementType) {
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
    }

    public String getName() {
        return name;
    }

    public double getDamage() {
        return damage;
    }

    public String getElementType() {
        return elementType;
    }

    @Override
    public String toString() {
        return "Card{name='" + name + "', damage=" + damage + ", elementType='" + elementType + "'}";
    }
}
