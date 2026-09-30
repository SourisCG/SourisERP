-- Suppliers and purchase orders
CREATE TABLE suppliers (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code       VARCHAR(20)  NOT NULL UNIQUE,
    name       VARCHAR(120) NOT NULL,
    tax_id     VARCHAR(30),
    email      VARCHAR(150),
    phone      VARCHAR(30),
    address    VARCHAR(255),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE SEQUENCE purchase_order_number_seq START 1 INCREMENT 1;

CREATE TABLE purchase_orders (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    number      VARCHAR(30) NOT NULL UNIQUE,
    supplier_id BIGINT      NOT NULL REFERENCES suppliers (id),
    status      VARCHAR(20) NOT NULL,
    order_date  DATE        NOT NULL,
    notes       VARCHAR(500),
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    created_by  BIGINT,
    updated_by  BIGINT
);

CREATE INDEX idx_purchase_orders_supplier ON purchase_orders (supplier_id);
CREATE INDEX idx_purchase_orders_status ON purchase_orders (status);

CREATE TABLE purchase_order_lines (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES purchase_orders (id) ON DELETE CASCADE,
    product_id   BIGINT        NOT NULL REFERENCES products (id),
    warehouse_id BIGINT        NOT NULL REFERENCES warehouses (id),
    quantity     INT           NOT NULL CHECK (quantity > 0),
    unit_cost    NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_purchase_order_lines_order ON purchase_order_lines (order_id);
