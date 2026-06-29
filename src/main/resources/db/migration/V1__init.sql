CREATE TABLE tenants (
                         id UUID PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         webhook_url VARCHAR(500),
                         created_at TIMESTAMP NOT NULL
);

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       tenant_id UUID NOT NULL REFERENCES tenants(id),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       role VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_tenant_id ON users(tenant_id);