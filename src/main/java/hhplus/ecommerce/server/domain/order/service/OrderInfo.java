package hhplus.ecommerce.server.domain.order.service;

import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class OrderInfo {

    public record OrderDetail(
            Long id,
            LocalDateTime orderDateTime,
            OrderStatus status,
            Integer amount
    ) {
        public static OrderDetail from(Order order, Integer amount) {
            return new OrderDetail(
                    order.getId(),
                    order.getOrderDateTime(),
                    order.getStatus(),
                    amount
            );
        }
    }

    public record OrderItemDetail(
            Long id,
            String name,
            Integer price,
            Integer quantity
    ) {
    }

    public record OrderAndItemDetails(
            OrderDetail orderDetail,
            List<OrderItemDetail> orderItemDetails
    ) {
        public static OrderAndItemDetails from(Order order, List<OrderItem> orderItems) {
            List<OrderItemDetail> orderItemDetails = orderItems.stream()
                    .map(orderItem -> new OrderItemDetail(
                            orderItem.getId(),
                            orderItem.getName(),
                            orderItem.getPrice(),
                            orderItem.getQuantity()
                    ))
                    .toList();

            int amount = orderItemDetails.stream()
                    .mapToInt(orderItemDetail -> orderItemDetail.price() * orderItemDetail.quantity())
                    .sum();

            return new OrderAndItemDetails(
                    new OrderDetail(
                            order.getId(),
                            order.getOrderDateTime(),
                            order.getStatus(),
                            amount
                    ),
                    orderItemDetails
            );
        }
    }
}
