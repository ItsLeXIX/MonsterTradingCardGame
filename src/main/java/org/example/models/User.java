package org.example.models;

public class User {
    private int id;
    private String username;
    private String password;
    private String name;
    private String bio;
    private String image;
    private int coins;
    private int elo;
    private int wins;
    private int losses;

    // Full Constructor
    public User(int id, String username, String password, String name, String bio, String image, int coins, int elo, int wins, int losses) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.bio = bio;
        this.image = image;
        this.coins = coins;
        this.elo = elo;
        this.wins = wins;
        this.losses = losses;
    }

    // Simplified Constructor
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.coins = 20; // Default values
        this.elo = 100;
        this.wins = 0;
        this.losses = 0;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getBio() { return bio; }
    public String getImage() { return image; }
    public int getCoins() { return coins; }
    public int getElo() { return elo; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setBio(String bio) { this.bio = bio; }
    public void setImage(String image) { this.image = image; }
    public void setCoins(int coins) { this.coins = coins; }
    public void setElo(int elo) { this.elo = elo; }
    public void setWins(int wins) { this.wins = wins; }
    public void setLosses(int losses) { this.losses = losses; }
}