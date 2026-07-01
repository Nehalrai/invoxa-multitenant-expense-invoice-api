CREATE TABLE expenses (
                          id UUID PRIMARY KEY,
                          tenant_id UUID NOT NULL REFERENCES tenants(id),
                          submitted_by UUID NOT NULL REFERENCES users(id),
                          amount NUMERIC(12,2) NOT NULL,
                          category VARCHAR(100) NOT NULL,
                          description TEXT,
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_expenses_tenant_id ON expenses(tenant_id);
CREATE INDEX idx_expenses_submitted_by ON expenses(submitted_by);