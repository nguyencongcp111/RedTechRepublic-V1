-- Người đăng (username lấy từ token) và gian hàng của sản phẩm.
-- Sản phẩm cũ chuyển sang tài khoản testadmin và "Shop Hệ Thống"
-- (id cố định, được tạo cùng lúc trong inventory_service V2__shops.sql).
ALTER TABLE products ADD COLUMN IF NOT EXISTS poster VARCHAR(64);
ALTER TABLE products ADD COLUMN IF NOT EXISTS shop_id UUID;
ALTER TABLE products ADD COLUMN IF NOT EXISTS shop_name VARCHAR(255);

UPDATE products SET poster = 'testadmin' WHERE poster IS NULL OR poster = '';
UPDATE products
SET shop_id = '00000000-0000-0000-0000-000000000001',
    shop_name = 'Shop Hệ Thống'
WHERE shop_id IS NULL;

ALTER TABLE products ALTER COLUMN poster SET NOT NULL;
ALTER TABLE products ALTER COLUMN shop_id SET NOT NULL;
ALTER TABLE products ALTER COLUMN shop_name SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_products_poster ON products (poster);
CREATE INDEX IF NOT EXISTS idx_products_shop ON products (shop_id);
