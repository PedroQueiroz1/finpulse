CREATE TABLE audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    action      VARCHAR(100) NOT NULL,
    user_email  VARCHAR(255),
    correlation_id VARCHAR(64),
    method_name VARCHAR(150) NOT NULL,
    success     BOOLEAN NOT NULL,
    error_message VARCHAR(500),
    duration_ms BIGINT,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_action     ON audit_logs(action);
CREATE INDEX idx_audit_logs_user_email ON audit_logs(user_email);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
