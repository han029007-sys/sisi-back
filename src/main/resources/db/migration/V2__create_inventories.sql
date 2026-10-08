CREATE TABLE inventories
(
    id               BIGSERIAL PRIMARY KEY,
    user_id          UUID NOT NULL,

    market_hash_name VARCHAR(255) NOT NULL,
    image_url        TEXT,
    price            NUMERIC(19, 2) NOT NULL,

    status           VARCHAR(30) NOT NULL,

    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventories_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);

CREATE INDEX idx_inventories_user_id
    ON inventories(user_id);

CREATE INDEX idx_inventories_user_status
    ON inventories(user_id, status);