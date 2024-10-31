package hhplus.ecommerce.server.domain.order.service;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.exception.NoSuchOrderException;
import hhplus.ecommerce.server.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Transactional
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public List<Order> findAllByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId);
    }

    public Order getOrder(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId).orElseThrow(NoSuchOrderException::new);
    }

    public List<OrderItem> findOrderItems(Long orderId) {
        return orderItemRepository.findAllByOrderId(orderId);
    }

    public Map<Long, Integer> findOrderAmounts(List<Long> orderIds) {
        return orderItemRepository.findOrderAmounts(orderIds);
    }

    public Order createOrderAndItems(OrderCommand.CreateOrder command, User user, List<Item> items) {
        Order order = orderRepository.save(command.toOrder(user));
        orderItemRepository.saveAll(command.toOrderItems(items, order));
        return order;
    }

    public void cancelOrder(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);
        orderItemRepository.deleteAll(orderItems);
        orderRepository.deleteById(orderId);
    }
}
