CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    steam_id   VARCHAR(30) NOT NULL UNIQUE,
    username   VARCHAR(255),
    avatar_url TEXT,
    balance    NUMERIC(19, 2) NOT NULL DEFAULT 0,
    first_deposit_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);