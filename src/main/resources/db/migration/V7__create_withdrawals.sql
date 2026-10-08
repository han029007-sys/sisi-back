CREATE TABLE withdrawals
(
    id                  BIGSERIAL PRIMARY KEY,
    user_id             UUID NOT NULL,
    inventory_id        BIGINT NOT NULL,

    project_id          VARCHAR(100) NOT NULL UNIQUE,

    market_hash_name    VARCHAR(255) NOT NULL,
    provider_listing_id VARCHAR(100),
    price               NUMERIC(19, 2) NOT NULL,

    status              VARCHAR(30) NOT NULL,

    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_withdrawals_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT fk_withdrawals_inventory
        FOREIGN KEY (inventory_id) REFERENCES inventories(id)
);

CREATE INDEX idx_withdrawals_user_id
    ON withdrawals(user_id);

CREATE INDEX idx_withdrawals_status
    ON withdrawals(status);