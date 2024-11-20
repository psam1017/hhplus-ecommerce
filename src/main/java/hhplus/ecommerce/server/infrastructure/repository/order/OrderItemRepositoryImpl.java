package hhplus.ecommerce.server.infrastructure.repository.order;

import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Repository
public class OrderItemRepositoryImpl implements OrderItemRepository {

    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public List<OrderItem> saveAll(List<OrderItem> orderItems) {
        return orderItemJpaRepository.saveAll(orderItems);
    }

    @Override
    public List<OrderItem> findAllByOrderId(Long orderId) {
        return orderItemJpaRepository.findAllByOrderId(orderId);
    }

    @Override
    public Map<Long, Integer> findOrderAmounts(List<Long> orderIds) {
        List<Object[]> results = orderItemJpaRepository.findOrderAmounts(orderIds);

        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> ((Number) result[1]).intValue()
                ));
    }

    @Override
    public void deleteAll(List<OrderItem> orderItems) {
        orderItemJpaRepository.deleteAll(orderItems);
    }

    @Override
    public List<Long> findTopItemIds(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return orderItemJpaRepository.findTopItemIds(startDateTime, endDateTime);
    }
}
