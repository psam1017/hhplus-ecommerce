package hhplus.ecommerce.server.domain.order;

import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class OrderInfo {

    public record OrderDetail(
            Long id,
            LocalDateTime orderDateTime,
            String title,
            OrderStatus status,
            Integer amount
    ) {
    }

    public record OrderItemDetail(
            Long id,
            String name,
            Integer price,
            Integer quantity
    ) {
    }

    public record OrderAndItemDetail(
            OrderDetail orderDetail,
            List<OrderItemDetail> orderItemDetail
    ) {
    }
}
