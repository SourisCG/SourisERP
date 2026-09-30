-- Customer invoices and payments
CREATE SEQUENCE invoice_number_seq START 1 INCREMENT 1;

CREATE TABLE invoices (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    number         VARCHAR(30) NOT NULL UNIQUE,
    sales_order_id BIGINT      NOT NULL UNIQUE REFERENCES sales_orders (id),
    customer_id    BIGINT      NOT NULL REFERENCES customers (id),
    status         VARCHAR(20) NOT NULL,
    issue_date     DATE        NOT NULL,
    due_date       DATE        NOT NULL,
    subtotal       NUMERIC(12, 2) NOT NULL,
    tax_total      NUMERIC(12, 2) NOT NULL,
    total          NUMERIC(12, 2) NOT NULL,
    paid_amount    NUMERIC(12, 2) NOT NULL DEFAULT 0,
    version        BIGINT      NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    created_by     BIGINT,
    updated_by     BIGINT
);

CREATE INDEX idx_invoices_customer ON invoices (customer_id);
CREATE INDEX idx_invoices_status ON invoices (status);
CREATE INDEX idx_invoices_issue_date ON invoices (issue_date);

CREATE TABLE invoice_lines (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    invoice_id BIGINT        NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    product_id BIGINT        NOT NULL REFERENCES products (id),
    quantity   INT           NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL,
    tax_rate   NUMERIC(5, 2)  NOT NULL
);

CREATE INDEX idx_invoice_lines_invoice ON invoice_lines (invoice_id);

CREATE TABLE invoice_payments (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    invoice_id BIGINT        NOT NULL REFERENCES invoices (id) ON DELETE CASCADE,
    amount     NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    method     VARCHAR(20)   NOT NULL,
    reference  VARCHAR(60),
    paid_at    TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_invoice_payments_invoice ON invoice_payments (invoice_id);
