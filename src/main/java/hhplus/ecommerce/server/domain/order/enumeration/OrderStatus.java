package hhplus.ecommerce.server.domain.order.enumeration;

import java.util.Objects;

public enum OrderStatus {

    ORDERED("주문완료"),
    ALL_CANCELLED("전체취소"),
    PARTIAL_CANCELLED("부분취소"),
    DELETED("주문내역삭제")
    ;

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String key() {
        return this.name();
    }

    public String value() {
        return this.value;
    }

    public static OrderStatus ofNullable(String str) {
        for (OrderStatus e : OrderStatus.values()) {
            if (Objects.equals(str, e.key()) || Objects.equals(str, e.value())) {
                return e;
            }
        }
        return null;
    }
}
