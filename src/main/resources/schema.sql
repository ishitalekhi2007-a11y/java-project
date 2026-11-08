CREATE TABLE IF NOT EXISTS restaurant_tables (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    table_number INT         NOT NULL UNIQUE,
    capacity     INT         NOT NULL CHECK (capacity > 0),
    status       VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS menu_items (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100)  NOT NULL UNIQUE,
    category  VARCHAR(30)   NOT NULL,
    price     DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    available BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS orders (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    table_id   INT         NOT NULL REFERENCES restaurant_tables(id),
    status     VARCHAR(20) NOT NULL,
    created_at TIMESTAMP   NOT NULL
);

CREATE TABLE IF NOT EXISTS order_items (
    order_id     INT           NOT NULL REFERENCES orders(id),
    menu_item_id INT           NOT NULL REFERENCES menu_items(id),
    quantity     INT           NOT NULL CHECK (quantity > 0),
    unit_price   DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (order_id, menu_item_id)
);

CREATE TABLE IF NOT EXISTS bills (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    order_id       INT           NOT NULL UNIQUE REFERENCES orders(id),
    subtotal       DECIMAL(10,2) NOT NULL,
    discount       DECIMAL(10,2) NOT NULL,
    discount_desc  VARCHAR(100),
    tax            DECIMAL(10,2) NOT NULL,
    tax_desc       VARCHAR(255),
    total          DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20)   NOT NULL,
    payment_method VARCHAR(20),
    created_at     TIMESTAMP     NOT NULL,
    paid_at        TIMESTAMP
);
