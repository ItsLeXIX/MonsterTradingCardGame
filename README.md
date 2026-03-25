# Monster Trading Card Game (MTCG)

A RESTful HTTP server for the Monster Trading Card Game, implementing card trading, package acquisition, deck building, and player battles.

## Overview

Monster Trading Card Game is a Java-based HTTP server that allows users to:
- Register and login
- Buy card packages
- Build custom decks
- Trade cards with other players
- Battle against other players
- View stats and scoreboards

## Technology Stack

- **Java 17** - Programming language
- **Maven** - Build tool and dependency management
- **PostgreSQL** - Database for persistent storage
- **Jackson** - JSON parsing
- **JUnit 5** - Testing framework
- **JWT** - Authentication tokens

## Prerequisites

Before running the application, ensure you have:

1. **Java 17 or higher** installed
2. **Maven** installed
3. **PostgreSQL** running on localhost:5432
4. **Database `mctg`** created in PostgreSQL

### Setting up PostgreSQL

```bash
# Create the database
psql -U postgres -c "CREATE DATABASE mctg;"

# Run the schema script
psql mctg -f DateBase.sql
```

## Building and Running

### 1. Clone and Navigate

```bash
cd /path/to/MonsterTradingCardGame
```

### 2. Build the Project

```bash
mvn clean compile
```

### 3. Start the Server

```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

The server will start on **port 10001**.

You should see:
```
Connected to database: jdbc:postgresql://localhost:5432/mctg
Database connection successful!
Server started on port 10001
```

### 4. Stop the Server

Press `Ctrl+C` or run:
```bash
pkill -f "org.example.Main"
```

## Testing

Run the provided test script:

```bash
./MonsterTradingCards.sh
```

Or with pause between sections:
```bash
./MonsterTradingCards.sh pause
```

### Starting Fresh

To reset the database and start from scratch:

```bash
# Clean the database
psql mctg -c "DELETE FROM deck; DELETE FROM trades; DELETE FROM package_cards; DELETE FROM packages; DELETE FROM cards; DELETE FROM users;"

# Run tests again
./MonsterTradingCards.sh
```

## API Endpoints

### User Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users` | Register a new user | No |
| POST | `/sessions` | Login and get token | No |
| GET | `/users/{username}` | Get user profile data | Yes |
| PUT | `/users/{username}` | Update user profile | Yes |

### Packages and Cards

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/packages` | Create a new package (admin only) | Yes (admin) |
| POST | `/transactions/packages` | Buy a package | Yes |
| GET | `/cards` | List all user's cards | Yes |

### Deck Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/deck` | Get configured deck | Yes |
| GET | `/deck?format=plain` | Get deck in plain text | Yes |
| PUT | `/deck` | Configure deck with 4 cards | Yes |

### Game Features

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/stats` | Get user's stats (ELO, Wins, Losses) | Yes |
| GET | `/scoreboard` | Get scoreboard sorted by ELO | Yes |
| POST | `/battles` | Enter battle lobby | Yes |

### Trading

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/tradings` | List all trading deals | Yes |
| POST | `/tradings` | Create a trading deal | Yes |
| DELETE | `/tradings/{id}` | Delete a trading deal | Yes |
| POST | `/tradings/{id}` | Accept a trading deal | Yes |

## Authentication

The API uses Bearer token authentication. After login, include the token in the Authorization header:

```bash
Authorization: Bearer kienboec-mtcgToken
```

For admin operations, use:
```bash
Authorization: Bearer admin-mtcgToken
```

## Example Usage

### Register a User
```bash
curl -X POST http://localhost:10001/users \
  -H "Content-Type: application/json" \
  -d '{"Username":"kienboec", "Password":"daniel"}'
```

### Login
```bash
curl -X POST http://localhost:10001/sessions \
  -H "Content-Type: application/json" \
  -d '{"Username":"kienboec", "Password":"daniel"}'
```

### Create Package (Admin)
```bash
curl -X POST http://localhost:10001/packages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer admin-mtcgToken" \
  -d '[{"Id":"card-uuid", "Name":"WaterGoblin", "Damage":10.0}, ...]'
```

### Buy Package
```bash
curl -X POST http://localhost:10001/transactions/packages \
  -H "Authorization: Bearer kienboec-mtcgToken"
```

### View Cards
```bash
curl http://localhost:10001/cards \
  -H "Authorization: Bearer kienboec-mtcgToken"
```

### Configure Deck
```bash
curl -X PUT http://localhost:10001/deck \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer kienboec-mtcgToken" \
  -d '["card-uuid-1", "card-uuid-2", "card-uuid-3", "card-uuid-4"]'
```

### Battle
```bash
curl -X POST http://localhost:10001/battles \
  -H "Authorization: Bearer kienboec-mtcgToken"
```

## Project Structure

```
src/
├── main/java/org/example/
│   ├── Main.java                 # Application entry point
│   ├── battle/
│   │   └── Battle.java           # Battle logic
│   ├── controllers/
│   │   ├── PackageController.java
│   │   ├── TradeController.java
│   │   ├── TransactionController.java
│   │   └── UserController.java
│   ├── core/
│   │   ├── HttpServer.java       # HTTP server
│   │   ├── RequestHandler.java   # Request handling
│   │   └── Router.java           # Route definitions
│   ├── dtos/
│   │   ├── AuthResponse.java
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── Request.java
│   │   └── Response.java
│   ├── models/
│   │   ├── Card.java
│   │   ├── MonsterCard.java
│   │   ├── Package.java
│   │   ├── SpellCard.java
│   │   ├── Trade.java
│   │   └── User.java
│   ├── repositories/
│   │   ├── CardRepository.java
│   │   ├── DeckRepository.java
│   │   ├── PackageRepository.java
│   │   ├── TradeRepository.java
│   │   └── UserRepository.java
│   ├── services/
│   │   ├── PackageService.java
│   │   ├── TradeService.java
│   │   ├── TransactionService.java
│   │   └── UserService.java
│   └── util/
│       ├── DatabaseUtil.java     # Database connection
│       └── JwtUtil.java          # JWT utilities
└── test/java/org/example/test/
    ├── BattleTest.java
    ├── PackageRepositoryTest.java
    ├── TransactionControllerTest.java
    └── UserControllerTest.java
```

## Database Schema

The application uses the following tables:

- **users** - User accounts with credentials, profile, coins, ELO, stats
- **cards** - Card definitions with type, element, damage, owner
- **packages** - Card packages available for purchase
- **package_cards** - Junction table for package contents
- **deck** - User's configured deck (4 cards)
- **trades** - Active trading deals
- **battles** - Battle records

## Battle Rules

Battles follow these rules:
- Each player needs exactly 4 cards in their deck
- Cards are played randomly from the deck
- Monster cards vs Spell cards have elemental advantages
- Special rules exist (e.g., Goblins are too afraid of Dragons to attack)
- Winner gains +10 ELO, loser loses -5 ELO
- Max 100 rounds, after which cards are removed randomly (fatigue)

## Trading Rules

- Users can only trade cards they own
- Cards in deck cannot be traded
- Trading deals specify required card type (monster/spell) and minimum damage
- Self-trading is not allowed

## Troubleshooting

### "Address already in use" Error
The server is already running. Stop it first:
```bash
pkill -f "org.example.Main"
```

### Database Connection Failed
Ensure PostgreSQL is running:
```bash
pg_ctl -D /opt/homebrew/var/postgresql@14 status
```

Or start it:
```bash
pg_ctl -D /opt/homebrew/var/postgresql@14 start
```

### Permission Denied on Test Script
Make the script executable:
```bash
chmod +x MonsterTradingCards.sh
```

## License

This project is part of the BIF3 curriculum.

## Original Repository

https://github.com/ItsLeXIX/MonsterTradingCardGame
