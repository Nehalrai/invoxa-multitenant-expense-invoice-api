CREATE TABLE clients (
                         id UUID PRIMARY KEY,
                         tenant_id UUID NOT NULL REFERENCES tenants(id),
                         name VARCHAR(255) NOT NULL,
                         email VARCHAR(255),
                         phone VARCHAR(50),
                         address TEXT,
                         created_at TIMESTAMP NOT NULL
);

CREATE TABLE invoices (
                          id UUID PRIMARY KEY,
                          tenant_id UUID NOT NULL REFERENCES tenants(id),
                          client_id UUID NOT NULL REFERENCES clients(id),
                          invoice_number VARCHAR(100) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          total_amount NUMERIC(12,2) NOT NULL,
                          due_date DATE,
                          notes TEXT,
                          stripe_payment_link VARCHAR(500),
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP NOT NULL
);

CREATE TABLE invoice_line_items (
                                    id UUID PRIMARY KEY,
                                    invoice_id UUID NOT NULL REFERENCES invoices(id),
                                    description VARCHAR(500) NOT NULL,
                                    quantity INTEGER NOT NULL,
                                    unit_price NUMERIC(12,2) NOT NULL,
                                    amount NUMERIC(12,2) NOT NULL
);

CREATE INDEX idx_clients_tenant_id ON clients(tenant_id);
CREATE INDEX idx_invoices_tenant_id ON invoices(tenant_id);
CREATE INDEX idx_invoices_client_id ON invoices(client_id);
CREATE INDEX idx_invoice_line_items_invoice_id ON invoice_line_items(invoice_id);