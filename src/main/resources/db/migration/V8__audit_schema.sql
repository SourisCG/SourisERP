-- Audit trail with JSONB snapshots
CREATE TABLE audit_log (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    entity_type VARCHAR(60) NOT NULL,
    action      VARCHAR(60) NOT NULL,
    entity_ref  VARCHAR(60),
    username    VARCHAR(80),
    user_id     BIGINT,
    after_json  JSONB,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_audit_log_created ON audit_log (created_at DESC);
CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, action);
