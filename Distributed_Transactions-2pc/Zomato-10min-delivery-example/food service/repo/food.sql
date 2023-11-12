CREATE TABLE food (
    id SERIAL PRIMARY KEY,
    name varchar(100) NOT NULL,
);

INSERT INTO food (name)
VALUES
('BURGER')