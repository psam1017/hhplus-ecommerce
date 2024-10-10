CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 ID',
    username VARCHAR(255) NOT NULL COMMENT '사용자 이름',
    refresh_token VARCHAR(255) COMMENT '리프레시 토큰',
    deleted_date_time DATETIME COMMENT '삭제 일시',
    created_date_time DATETIME NOT NULL COMMENT '생성 일시',
    modified_date_time DATETIME NOT NULL COMMENT '수정 일시'
);

CREATE TABLE user_points (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '사용자 포인트 ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '사용자 ID',
    amount INT NOT NULL COMMENT '포인트 금액',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE point_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '포인트 내역 ID',
    user_id BIGINT NOT NULL COMMENT '사용자 ID',
    original_balance INT NOT NULL COMMENT '변경 전 잔액',
    change_amount INT NOT NULL COMMENT '변경 금액',
    reason VARCHAR(255) COMMENT '변경 사유',
    created_date_time DATETIME NOT NULL COMMENT '생성 일시',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '상품 ID',
    name VARCHAR(255) NOT NULL COMMENT '상품명',
    price INT NOT NULL COMMENT '상품 가격',
    description TEXT COMMENT '상품 상세설명',
    deleted_date_time DATETIME COMMENT '삭제 일시',
    created_date_time DATETIME NOT NULL COMMENT '생성 일시',
    modified_date_time DATETIME NOT NULL COMMENT '수정 일시'
);

CREATE TABLE item_stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '상품 재고 ID',
    item_id BIGINT NOT NULL UNIQUE COMMENT '상품 ID',
    amount INT NOT NULL COMMENT '재고 수량',
    FOREIGN KEY (item_id) REFERENCES items(id)
);

CREATE TABLE top_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '인기 상품 ID',
    item_id BIGINT NOT NULL COMMENT '상품 ID',
    standard_date DATE NOT NULL COMMENT '기준 날짜',
    counting_period INT NOT NULL COMMENT '집계 기간(일)',
    sales_amount INT NOT NULL COMMENT '판매 수량',
    FOREIGN KEY (item_id) REFERENCES items(id)
);

CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '주문 ID',
    user_id BIGINT NOT NULL COMMENT '유저 ID',
    order_date_time DATETIME NOT NULL COMMENT '주문 일시',
    title VARCHAR(255) COMMENT '주문 제목',
    status VARCHAR(50) NOT NULL COMMENT '주문 상태',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '주문 상품 ID',
    order_id BIGINT NOT NULL COMMENT '주문 ID',
    item_id BIGINT NOT NULL COMMENT '상품 ID',
    price INT NOT NULL COMMENT '상품 가격',
    quantity INT NOT NULL COMMENT '수량',
    sales_amount INT NOT NULL COMMENT '판매 금액 = 상품 가격 * 수량',
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (item_id) REFERENCES items(id)
);
