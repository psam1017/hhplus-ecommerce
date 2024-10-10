package hhplus.ecommerce.server.domain.order.enumeration;

import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

import java.util.Objects;

public enum OrderStatus implements Documentable {

    // 주문실패, 배송관련 상태 추가 가능
    ORDERED("주문완료"),
    ALL_CANCELLED("전체취소"),
    PARTIAL_CANCELLED("부분취소"),
    DELETED("주문내역삭제")
    ;

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    @Override
    public String key() {
        return this.name();
    }

    @Override
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
