-- Hồ sơ người bán: vừa là yêu cầu ứng tuyển (PENDING/REJECTED),
-- vừa là thông tin Seller của user đã được duyệt (APPROVED).
CREATE TABLE IF NOT EXISTS info_sellers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(16) NOT NULL,
    reject_reason VARCHAR(500),
    reviewed_by VARCHAR(64),
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_info_sellers_user FOREIGN KEY (user_id) REFERENCES system_users (id),
    CONSTRAINT ck_info_sellers_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_info_sellers_status ON info_sellers (status);
