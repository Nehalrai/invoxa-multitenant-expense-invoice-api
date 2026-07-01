CREATE TABLE audit_logs (
                            id UUID PRIMARY KEY,
                            tenant_id UUID NOT NULL,
                            actor_user_id UUID NOT NULL,
                            actor_email VARCHAR(255) NOT NULL,
                            action VARCHAR(100) NOT NULL,
                            entity_type VARCHAR(100) NOT NULL,
                            entity_id UUID NOT NULL,
                            metadata TEXT,
                            created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_audit_logs_tenant_id ON audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);