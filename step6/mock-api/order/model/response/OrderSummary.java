package hhplus.ecommerce.server.interfaces.order.model.response;

import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.interfaces.common.jsonformat.KoreanDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class OrderSummary {

    private Long id;
    private String title;
    @KoreanDateTime
    private LocalDateTime orderDateTime;
    private OrderStatus orderStatus;
    private String orderStatusValue;

    @Builder
    protected OrderSummary(Long id, String title, LocalDateTime orderDateTime, OrderStatus orderStatus, String orderStatusValue) {
        this.id = id;
        this.title = title;
        this.orderDateTime = orderDateTime;
        this.orderStatus = orderStatus;
        this.orderStatusValue = orderStatusValue;
    }
}
