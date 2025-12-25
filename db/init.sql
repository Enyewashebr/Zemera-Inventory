-- Schema for Zemera Cafeteria Inventory & Sales System

CREATE TABLE IF NOT EXISTS product (
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(150) NOT NULL UNIQUE,
    category        VARCHAR(50)  NOT NULL, -- Drink / Food
    unit            VARCHAR(30)  NOT NULL, -- kg / L / pcs / plate / none
    buying_price    NUMERIC(12,2) NOT NULL CHECK (buying_price >= 0),
    selling_price   NUMERIC(12,2) NOT NULL CHECK (selling_price >= 0),
    current_stock   NUMERIC(14,3) NOT NULL DEFAULT 0 CHECK (current_stock >= 0),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS purchase (
    id              SERIAL PRIMARY KEY,
    product_id      INT NOT NULL REFERENCES product(id),
    quantity        NUMERIC(14,3) NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    purchase_date   DATE NOT NULL,
    total_cost      NUMERIC(14,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS "order" (
    id              SERIAL PRIMARY KEY,
    order_code      VARCHAR(50) UNIQUE, -- human friendly order id for ticket
    waiter_name     VARCHAR(120) NOT NULL,
    order_datetime  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    total_amount    NUMERIC(14,2),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_item (
    id              SERIAL PRIMARY KEY,
    order_id        INT NOT NULL REFERENCES "order"(id) ON DELETE CASCADE,
    product_id      INT NOT NULL REFERENCES product(id),
    quantity        NUMERIC(14,3) NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
    line_total      NUMERIC(14,2) GENERATED ALWAYS AS (quantity * unit_price) STORED
);

-- Simple trigger to keep product.updated_at in sync
CREATE OR REPLACE FUNCTION set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_product_set_timestamp ON product;
CREATE TRIGGER trg_product_set_timestamp
BEFORE UPDATE ON product
FOR EACH ROW
EXECUTE FUNCTION set_timestamp();








