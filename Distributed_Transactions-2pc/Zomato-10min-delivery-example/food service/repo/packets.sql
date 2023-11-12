CREATE TABLE packets (
    id SERIAL PRIMARY KEY,
    food_id INT,
    is_reserved BOOLEAN,
    order_id INT,
    reservation_expiry BIGINT,
    order_expiry BIGINT
);
