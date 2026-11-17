-- Gian hàng. Chỉ ADMIN và SELLER được tạo (kiểm tra ở API).
CREATE TABLE IF NOT EXISTS shops (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    owner_username VARCHAR(64) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_shops_owner ON shops (owner_username);
CREATE INDEX IF NOT EXISTS idx_shops_name ON shops (lower(name));

-- "Shop Hệ Thống" của testadmin. Id cố định để product_service V4 gán cho sản phẩm cũ.
INSERT INTO shops (id, name, address, phone, email, owner_username, version, created_at, updated_at)
VALUES ('00000000-0000-0000-0000-000000000001', 'Shop Hệ Thống', 'TechShop', '0000000000',
        'system@techshop.local', 'testadmin', 0, now(), now())
ON CONFLICT (id) DO NOTHING;
