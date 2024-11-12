# noinspection SqlWithoutWhereForFile
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

INSERT INTO items (name, price, status)
WITH RECURSIVE item_cte (n) AS
(
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM item_cte WHERE n < 1000000
)
SELECT
    CONCAT('Title', LPAD(n, 7, '0')) AS name,
    FLOOR(RAND() * 1000) AS price,
    'ACTIVE' AS status
FROM item_cte;

INSERT INTO item_stocks (item_id, amount)
SELECT id, FLOOR(RAND() * 100) + 10 FROM items;

INSERT INTO orders (user_id, order_date_time, status)
WITH RECURSIVE order_cte (n) AS
(
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM order_cte WHERE n < 100000
)
SELECT
    @user_id,
    DATE_SUB(NOW(), INTERVAL 1 DAY) AS order_date_time,
    'ORDERED' AS status
FROM order_cte;

INSERT INTO order_items (order_id, item_id, name, price, quantity, total_amount)
SELECT
    o.id AS order_id,
    i.id AS item_id,
    i.name,
    i.price,
    @quantity := FLOOR(RAND() * 10) + 1 AS quantity,
    i.price * @quantity AS total_amount
FROM
    orders o
        JOIN
    items i ON i.id BETWEEN (o.id - 1) * 10 + 1 AND o.id * 10
WHERE
    o.id <= 100000;