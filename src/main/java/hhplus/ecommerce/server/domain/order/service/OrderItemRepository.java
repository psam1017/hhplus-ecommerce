package hhplus.ecommerce.server.domain.order.service;

import hhplus.ecommerce.server.domain.order.OrderItem;

import java.util.List;
import java.util.Map;

public interface OrderItemRepository {

    List<OrderItem> saveAll(List<OrderItem> orderItems);

    List<OrderItem> findAllByOrderId(Long orderId);

    Map<Long, Integer> findOrderAmounts(List<Long> orderIds);
}
