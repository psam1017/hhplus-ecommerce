package hhplus.ecommerce.server.domain.order.service;

import hhplus.ecommerce.server.domain.order.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    List<Order> findAllByUserId(Long userId);

    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    void deleteById(Long orderId);
}
