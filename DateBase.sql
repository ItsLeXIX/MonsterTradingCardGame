-- SQL script to recreate the Monster Trading Card Game (MTCG) database schema

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- USERS table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    bio TEXT,
    image TEXT,
    coins INTEGER DEFAULT 20,
    elo INTEGER DEFAULT 100,
    wins INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0
);

-- CARDS table
CREATE TABLE cards (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    damage DOUBLE PRECISION NOT NULL,
    type VARCHAR(10) CHECK (type IN ('monster', 'spell')),
    element VARCHAR(10),
    status VARCHAR(10) DEFAULT 'inventory' CHECK (status IN ('inventory', 'deck', 'trade')),
    owner_id UUID REFERENCES users(id) ON DELETE SET NULL
);

-- PACKAGES table
CREATE TABLE packages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    status VARCHAR(10) NOT NULL DEFAULT 'available',
    buyer UUID,
    FOREIGN KEY (buyer) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_package_status ON packages(status);

-- PACKAGE_CARDS table
CREATE TABLE package_cards (
    package_id UUID NOT NULL,
    card_id UUID NOT NULL,
    PRIMARY KEY (package_id, card_id),
    FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

CREATE INDEX idx_package_cards ON package_cards(package_id, card_id);

-- DECK table
CREATE TABLE deck (
    user_id UUID NOT NULL,
    card_id UUID NOT NULL,
    PRIMARY KEY (user_id, card_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- TRADES table
CREATE TABLE trades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id UUID NOT NULL,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- BATTLES table
CREATE TABLE battles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid()
);
