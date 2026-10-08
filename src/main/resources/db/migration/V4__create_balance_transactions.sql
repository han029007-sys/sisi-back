CREATE TABLE balance_transactions
(
    id         BIGSERIAL PRIMARY KEY,
    user_id    UUID NOT NULL,
    type       VARCHAR(30) NOT NULL,
    amount     NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_balance_transactions_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);

CREATE INDEX idx_balance_transactions_user_id
    ON balance_transactions(user_id);

CREATE INDEX idx_balance_transactions_created_at
    ON balance_transactions(created_at);