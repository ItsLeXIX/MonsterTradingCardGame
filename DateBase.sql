-- Create sequences
CREATE SEQUENCE battles_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE packages_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE SEQUENCE users_id_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;

-- Create users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    bio TEXT,
    image TEXT,
    coins INTEGER DEFAULT 20,
    elo INTEGER DEFAULT 100,
    wins INTEGER DEFAULT 0,
    losses INTEGER DEFAULT 0
);

-- Create cards table
CREATE TABLE cards (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    damage DOUBLE PRECISION NOT NULL,
    type VARCHAR(10) CHECK (type IN ('monster', 'spell')),
    element VARCHAR(10) CHECK (element IN ('fire', 'water', 'normal')),
    owner_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(10) DEFAULT 'inventory' CHECK (status IN ('inventory', 'deck', 'trade'))
);

-- Create decks table
CREATE TABLE decks (
    user_id INTEGER NOT NULL,
    card_id UUID NOT NULL,
    PRIMARY KEY (user_id, card_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Create packages table
CREATE TABLE packages (
    id SERIAL PRIMARY KEY,
    status VARCHAR(20) DEFAULT 'available'
);

-- Create package_cards table
CREATE TABLE package_cards (
    package_id INTEGER NOT NULL,
    card_id UUID NOT NULL,
    PRIMARY KEY (package_id, card_id),
    FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE,
    FOREIGN KEY (card_id) REFERENCES cards(id) ON DELETE CASCADE
);

-- Create trades table
CREATE TABLE trades (
    id UUID PRIMARY KEY,
    card_id UUID REFERENCES cards(id) ON DELETE CASCADE,
    required_type VARCHAR(10) CHECK (required_type IN ('monster', 'spell')),
    required_min_damage DOUBLE PRECISION,
    owner_id INTEGER REFERENCES users(id) ON DELETE CASCADE
);

-- Create battles table
CREATE TABLE battles (
    id SERIAL PRIMARY KEY,
    player1_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    player2_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    winner_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    battle_log TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance optimization
CREATE INDEX idx_card_owner ON cards(owner_id);
CREATE INDEX idx_package_status ON packages(status);
CREATE INDEX idx_package_cards ON package_cards(package_id, card_id);

-- Constraints for referential integrity
ALTER TABLE battles ADD CONSTRAINT battles_player1_id_fkey FOREIGN KEY (player1_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE battles ADD CONSTRAINT battles_player2_id_fkey FOREIGN KEY (player2_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE battles ADD CONSTRAINT battles_winner_id_fkey FOREIGN KEY (winner_id) REFERENCES users(id) ON DELETE SET NULL;

-- Sequence ownership
ALTER SEQUENCE battles_id_seq OWNED BY battles.id;
ALTER SEQUENCE packages_id_seq OWNED BY packages.id;
ALTER SEQUENCE users_id_seq OWNED BY users.id;
