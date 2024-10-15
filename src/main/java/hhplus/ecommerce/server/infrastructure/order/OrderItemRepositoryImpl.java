package hhplus.ecommerce.server.infrastructure.order;

import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.service.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

        // 결과를 맵으로 변환하여 반환
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],  // 주문 ID
                        result -> ((Number) result[1]).intValue()  // 수량 합계
                ));
    }
}
