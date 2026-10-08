CREATE TABLE upgrades
(
    id                      BIGSERIAL PRIMARY KEY,

    user_id                 UUID NOT NULL,

    input_inventory_id      BIGINT NOT NULL,
    output_inventory_id     BIGINT,

    input_listing_id        VARCHAR(100),
    input_market_hash_name  VARCHAR(255) NOT NULL,
    input_image_url         TEXT,
    input_price             NUMERIC(19, 2) NOT NULL,

    target_listing_id       VARCHAR(100),
    target_market_hash_name VARCHAR(255) NOT NULL,
    target_image_url        TEXT,
    target_price            NUMERIC(19, 2) NOT NULL,

    chance                  NUMERIC(10, 8) NOT NULL,
    roll                    NUMERIC(10, 8) NOT NULL,
    result                  VARCHAR(20) NOT NULL,

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_upgrades_user
        FOREIGN KEY (user_id)
            REFERENCES users (id),

    CONSTRAINT fk_upgrades_input_inventory
        FOREIGN KEY (input_inventory_id)
            REFERENCES inventories (id),

    CONSTRAINT fk_upgrades_output_inventory
        FOREIGN KEY (output_inventory_id)
            REFERENCES inventories (id)
);

CREATE INDEX idx_upgrades_user_id
    ON upgrades(user_id);

CREATE INDEX idx_upgrades_created_at
    ON upgrades(created_at);

CREATE INDEX idx_upgrades_input_inventory_id
    ON upgrades(input_inventory_id);

CREATE INDEX idx_upgrades_output_inventory_id
    ON upgrades(output_inventory_id);