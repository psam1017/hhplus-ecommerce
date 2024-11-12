# noinspection SqlResolveForFile

CREATE DATABASE ecommerce DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;

CREATE TABLE carts (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '각 장바구니 항목의 고유 식별자',
    quantity INTEGER NOT NULL COMMENT '장바구니에 담긴 아이템의 수량',
    item_id BIGINT COMMENT '장바구니에 있는 아이템의 식별자',
    user_id BIGINT COMMENT '장바구니 소유자의 사용자 식별자',
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE item_stocks (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '각 아이템 재고 항목의 고유 식별자',
    amount INTEGER NOT NULL COMMENT '해당 아이템의 재고 수량',
    item_id BIGINT COMMENT '이 재고 항목이 연결된 아이템의 식별자',
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE items (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '각 아이템의 고유 식별자',
    name VARCHAR(255) NOT NULL COMMENT '아이템의 이름',
    price INTEGER NOT NULL COMMENT '아이템의 가격',
    status VARCHAR(31) NOT NULL COMMENT '아이템의 상태',
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE order_items (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '각 주문 아이템 항목의 고유 식별자',
    name VARCHAR(255) NOT NULL COMMENT '주문 아이템의 이름',
    price INTEGER NOT NULL COMMENT '주문 시점의 아이템 가격',
    quantity INTEGER NOT NULL COMMENT '주문한 아이템의 수량',
    item_id BIGINT NOT NULL COMMENT '주문에 포함된 아이템의 식별자',
    order_id BIGINT NOT NULL COMMENT '이 항목이 포함된 주문의 식별자',
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_date_time DATETIME(6),
    status VARCHAR(63),
    user_id BIGINT,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE points (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '각 사용자 포인트 항목의 고유 식별자',
    amount INTEGER NOT NULL COMMENT '포인트가 연결된 사용자의 식별자',
    version BIGINT COMMENT '포인트 항목의 버전(낙관적 락)',
    user_id BIGINT COMMENT '사용자가 보유한 포인트 양',
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '각 사용자의 고유 식별자',
    username VARCHAR(255) COMMENT '사용자의 이름',
    PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE INDEX idx_carts_user_id ON carts (user_id);
CREATE INDEX idx_carts_item_id ON carts (item_id);
CREATE UNIQUE INDEX idx_item_stocks_item_id ON item_stocks (item_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_item_id ON order_items (item_id);
CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_points_user_id ON points (user_id);

# 성능 개선 인덱스 추가
CREATE INDEX idx_items_price ON items (price);
CREATE INDEX idx_orders_composite ON orders (status, order_date_time);
CREATE INDEX idx_order_items_composite ON order_items (order_id, item_id, total_amount);
