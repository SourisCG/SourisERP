-- Warehouses, stock levels and the stock movement ledger
CREATE TABLE warehouses (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code       VARCHAR(20) NOT NULL UNIQUE,
    name       VARCHAR(80) NOT NULL,
    address    VARCHAR(255),
    active     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE TABLE inventory_items (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id        BIGINT      NOT NULL REFERENCES products (id),
    warehouse_id      BIGINT      NOT NULL REFERENCES warehouses (id),
    quantity          INT         NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    reserved_quantity INT         NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    version           BIGINT      NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    created_by        BIGINT,
    updated_by        BIGINT,
    CONSTRAINT uq_inventory_product_warehouse UNIQUE (product_id, warehouse_id)
);

CREATE INDEX idx_inventory_warehouse ON inventory_items (warehouse_id);
CREATE INDEX idx_inventory_product ON inventory_items (product_id);

CREATE TABLE stock_movements (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id     BIGINT      NOT NULL REFERENCES products (id),
    warehouse_id   BIGINT      NOT NULL REFERENCES warehouses (id),
    type           VARCHAR(30) NOT NULL,
    quantity       INT         NOT NULL,
    reference_type VARCHAR(30),
    reference_id   BIGINT,
    notes          VARCHAR(255),
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,
    created_by     BIGINT,
    updated_by     BIGINT
);

CREATE INDEX idx_movements_product ON stock_movements (product_id, created_at DESC);
CREATE INDEX idx_movements_warehouse ON stock_movements (warehouse_id, created_at DESC);
