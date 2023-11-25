CREATE TABLE delivery (
    id SERIAL PRIMARY KEY,
    is_reserved BOOLEAN,
    order_id INT,
    reservation_expiry BIGINT,
    order_expiry BIGINT
);