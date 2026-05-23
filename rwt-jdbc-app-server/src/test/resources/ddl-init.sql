CREATE TABLE tb_product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    price NUMERIC(10, 2),
    producer VARCHAR(255),
    universal_product_code VARCHAR(255),
    country VARCHAR(255),
    entry_date DATE,
    amount INTEGER
);
