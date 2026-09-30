-- Customers and sales orders
CREATE TABLE customers (
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

CREATE SEQUENCE sales_order_number_seq START 1 INCREMENT 1;

CREATE TABLE sales_orders (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    number      VARCHAR(30) NOT NULL UNIQUE,
    customer_id BIGINT      NOT NULL REFERENCES customers (id),
    status      VARCHAR(20) NOT NULL,
    order_date  DATE        NOT NULL,
    notes       VARCHAR(500),
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    created_by  BIGINT,
    updated_by  BIGINT
);

CREATE INDEX idx_sales_orders_customer ON sales_orders (customer_id);
CREATE INDEX idx_sales_orders_status ON sales_orders (status);

CREATE TABLE sales_order_lines (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id     BIGINT        NOT NULL REFERENCES sales_orders (id) ON DELETE CASCADE,
    product_id   BIGINT        NOT NULL REFERENCES products (id),
    warehouse_id BIGINT        NOT NULL REFERENCES warehouses (id),
    quantity     INT           NOT NULL CHECK (quantity > 0),
    unit_price   NUMERIC(12, 2) NOT NULL,
    discount     NUMERIC(5, 2)  NOT NULL DEFAULT 0,
    tax_rate     NUMERIC(5, 2)  NOT NULL
);

CREATE INDEX idx_sales_order_lines_order ON sales_order_lines (order_id);
CREATE INDEX idx_sales_order_lines_product ON sales_order_lines (product_id);
