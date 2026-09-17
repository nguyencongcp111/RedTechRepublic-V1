CREATE TABLE IF NOT EXISTS inventory_items (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    sku VARCHAR(64) NOT NULL,
    on_hand INTEGER NOT NULL,
    reserved INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_inventory_stock CHECK (
        on_hand >= 0 AND reserved >= 0 AND reserved <= on_hand
    )
);

UPDATE inventory_items SET sku = upper(sku);
CREATE UNIQUE INDEX IF NOT EXISTS uk_inventory_product ON inventory_items (product_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_inventory_sku ON inventory_items (sku);

CREATE TABLE IF NOT EXISTS inventory_reservations (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    inventory_item_id UUID NOT NULL REFERENCES inventory_items(id),
    quantity INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_reservation_quantity CHECK (quantity > 0)
);

ALTER TABLE inventory_reservations ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_inventory_reservation_key
    ON inventory_reservations (idempotency_key);
CREATE INDEX IF NOT EXISTS idx_inventory_reservation_order
    ON inventory_reservations (order_id);
CREATE INDEX IF NOT EXISTS idx_inventory_reservation_expiry
    ON inventory_reservations (status, expires_at);
