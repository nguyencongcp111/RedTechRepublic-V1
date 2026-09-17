CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_quantity INTEGER NOT NULL,
    total_amount BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    failure_reason VARCHAR(1000),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_order_quantity CHECK (total_quantity > 0),
    CONSTRAINT ck_order_amount CHECK (total_amount >= 0)
);

ALTER TABLE orders ADD COLUMN IF NOT EXISTS request_hash VARCHAR(64);
UPDATE orders SET request_hash = repeat('0', 64) WHERE request_hash IS NULL;
ALTER TABLE orders ALTER COLUMN request_hash SET NOT NULL;
UPDATE orders SET currency = upper(currency);
CREATE UNIQUE INDEX IF NOT EXISTS uk_orders_idempotency_key ON orders (idempotency_key);

CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    inventory_reservation_id UUID,
    sku VARCHAR(64) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price BIGINT NOT NULL,
    line_total BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_order_item_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_item_price CHECK (unit_price >= 0 AND line_total >= 0)
);

ALTER TABLE order_items ADD COLUMN IF NOT EXISTS inventory_reservation_id UUID;
CREATE UNIQUE INDEX IF NOT EXISTS uk_order_item_product ON order_items (order_id, product_id);
CREATE UNIQUE INDEX IF NOT EXISTS uk_order_item_reservation
    ON order_items (inventory_reservation_id) WHERE inventory_reservation_id IS NOT NULL;
