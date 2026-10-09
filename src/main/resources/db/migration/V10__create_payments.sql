CREATE TABLE payments (
                          id UUID PRIMARY KEY,
                          user_id UUID NOT NULL REFERENCES users(id),
                          invoice_id VARCHAR(255) UNIQUE,
                          amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          paid_at TIMESTAMP
);

CREATE INDEX idx_payments_user_id
    ON payments(user_id);

CREATE INDEX idx_payments_status
    ON payments(status);