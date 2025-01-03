CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    bio TEXT,
    image TEXT,
    coins INT DEFAULT 20,
    elo INT DEFAULT 100,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0
);

CREATE TABLE cards (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    damage FLOAT NOT NULL,
    type VARCHAR(10) CHECK (type IN ('monster', 'spell')),
    element VARCHAR(10) CHECK (element IN ('fire', 'water', 'normal')),
    owner_id INT REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE decks (
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    card_id UUID REFERENCES cards(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, card_id)
);

CREATE TABLE packages (
    id SERIAL PRIMARY KEY
);

CREATE TABLE package_cards (
    package_id INT REFERENCES packages(id) ON DELETE CASCADE,
    card_id UUID REFERENCES cards(id) ON DELETE CASCADE,
    PRIMARY KEY (package_id, card_id)
);

CREATE TABLE trades (
    id UUID PRIMARY KEY,
    card_id UUID REFERENCES cards(id) ON DELETE CASCADE,
    required_type VARCHAR(10) CHECK (required_type IN ('monster', 'spell')),
    required_min_damage FLOAT,
    owner_id INT REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE battles (
    id SERIAL PRIMARY KEY,
    player1_id INT REFERENCES users(id) ON DELETE CASCADE,
    player2_id INT REFERENCES users(id) ON DELETE CASCADE,
    winner_id INT REFERENCES users(id) ON DELETE SET NULL,
    battle_log TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
