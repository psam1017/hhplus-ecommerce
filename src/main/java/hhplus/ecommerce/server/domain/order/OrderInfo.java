package hhplus.ecommerce.server.domain.order;

import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;

import java.time.LocalDateTime;

public class OrderInfo {

    public record OrderDetail(
            Long id,
            LocalDateTime orderDateTime,
            String title,
            OrderStatus status,
            Integer amount
    ) {
    }
}
