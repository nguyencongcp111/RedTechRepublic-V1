ALTER TABLE orders
ADD COLUMN payment_method VARCHAR(32);

UPDATE orders
SET payment_method = 'CASH_ON_DELIVERY'
WHERE payment_method IS NULL;

ALTER TABLE orders
ALTER COLUMN payment_method SET NOT NULL;