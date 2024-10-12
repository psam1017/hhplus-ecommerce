CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '각 사용자의 고유 식별자',
    username VARCHAR(255) NOT NULL COMMENT '사용자의 이름'
);

CREATE TABLE user_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '각 사용자 포인트 항목의 고유 식별자',
    user_id BIGINT UNIQUE NOT NULL COMMENT '포인트가 연결된 사용자의 식별자',
    amount INT NOT NULL COMMENT '사용자가 보유한 포인트 양'
);

CREATE TABLE items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '각 아이템의 고유 식별자',
    name VARCHAR(255) NOT NULL COMMENT '아이템의 이름',
    price INT NOT NULL COMMENT '아이템의 가격'
);

CREATE TABLE item_stocks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '각 아이템 재고 항목의 고유 식별자',
    item_id BIGINT UNIQUE NOT NULL COMMENT '이 재고 항목이 연결된 아이템의 식별자',
    amount INT NOT NULL COMMENT '해당 아이템의 재고 수량'
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '각 주문의 고유 식별자',
    user_id BIGINT NOT NULL COMMENT '주문을 한 사용자의 식별자',
    order_date_time DATETIME NOT NULL COMMENT '주문이 생성된 시간',
    status VARCHAR(50) NOT NULL COMMENT '주문의 현재 상태'
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '각 주문 아이템 항목의 고유 식별자',
    order_id BIGINT NOT NULL COMMENT '이 항목이 포함된 주문의 식별자',
    item_id BIGINT NOT NULL COMMENT '주문에 포함된 아이템의 식별자',
    name VARCHAR(255) NOT NULL COMMENT '주문 아이템의 이름',
    price INT NOT NULL COMMENT '주문 시점의 아이템 가격',
    quantity INT NOT NULL COMMENT '주문한 아이템의 수량'
);

CREATE TABLE carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '각 장바구니 항목의 고유 식별자',
    user_id BIGINT NOT NULL COMMENT '장바구니 소유자의 사용자 식별자',
    item_id BIGINT NOT NULL COMMENT '장바구니에 있는 아이템의 식별자',
    quantity INT NOT NULL COMMENT '장바구니에 담긴 아이템의 수량'
);