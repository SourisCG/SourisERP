-- Product catalog: categories and products
CREATE TABLE categories (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name        VARCHAR(80)  NOT NULL UNIQUE,
    description VARCHAR(255),
    parent_id   BIGINT REFERENCES categories (id),
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL,
    created_by  BIGINT,
    updated_by  BIGINT
);

CREATE INDEX idx_categories_parent ON categories (parent_id);

CREATE TABLE products (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sku         VARCHAR(40)   NOT NULL UNIQUE,
    name        VARCHAR(120)  NOT NULL,
    description VARCHAR(500),
    category_id BIGINT        NOT NULL REFERENCES categories (id),
    unit        VARCHAR(20)   NOT NULL,
    sale_price  NUMERIC(12, 2) NOT NULL,
    cost_price  NUMERIC(12, 2) NOT NULL,
    tax_rate    NUMERIC(5, 2)  NOT NULL DEFAULT 21,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    version     BIGINT        NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL,
    created_by  BIGINT,
    updated_by  BIGINT
);

CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_active ON products (active);
