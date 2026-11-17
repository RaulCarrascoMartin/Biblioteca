-- Refresh tokens opacos del módulo auth: solo se guarda el hash (SHA-256 hex), la caducidad y la revocación.
-- user_id es una relación por id con users (sin clave foránea entre módulos).
CREATE TABLE refresh_tokens (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL,
    token_hash    VARCHAR(64) NOT NULL,
    token_version INT         NOT NULL DEFAULT 0,
    expires_at    TIMESTAMP(6) NOT NULL,
    revoked_at    TIMESTAMP(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
