# noinspection SqlResolveForFile

USE ecommerce;

TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE item_stocks;
TRUNCATE TABLE items;
TRUNCATE TABLE users;

INSERT INTO users (username)
VALUES ('Alice');

SET @user_id = LAST_INSERT_ID();

SET SESSION cte_max_recursion_depth = 1000000;

INSERT INTO items (name, price)
WITH RECURSIVE cte (n) AS
                   (
                       SELECT 1
                       UNION ALL
                       SELECT n + 1 FROM cte WHERE n < 1000000
                   )
SELECT
    CONCAT('Title', LPAD(n, 7, '0')) AS name,
    FLOOR(RAND() * 1000) AS price
FROM cte;

INSERT INTO item_stocks (item_id, amount)
SELECT id, FLOOR(RAND() * 100) FROM items;

INSERT INTO orders (user_id, order_date_time, status)
VALUES (@user_id, DATE_SUB(NOW(), INTERVAL 1 DAY), 'ORDERED');

SET @order_id = LAST_INSERT_ID();

INSERT INTO order_items (order_id, item_id, name, price, quantity)
SELECT
    @order_id,
    items.id,
    items.name,
    items.price,
    FLOOR(RAND() * 10) + 1
FROM
    items;
