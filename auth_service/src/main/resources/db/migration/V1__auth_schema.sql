-- Baseline cho schema đã có sẵn (trước đây được tạo thủ công / từ seed).
CREATE TABLE IF NOT EXISTS system_users (
    id UUID PRIMARY KEY,
    username VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMPTZ NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES system_users (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_system_users_username ON system_users (username);
