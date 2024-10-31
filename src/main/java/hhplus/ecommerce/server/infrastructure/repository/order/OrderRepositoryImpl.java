package hhplus.ecommerce.server.infrastructure.repository.order;

import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.service.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public List<Order> findAllByUserId(Long userId) {
        return orderJpaRepository.findAllByUserId(userId);
    }

    @Override
    public Optional<Order> findByIdAndUserId(Long orderId, Long userId) {
        return orderJpaRepository.findByIdAndUserId(orderId, userId);
    }

    @Override
    public void deleteById(Long orderId) {
        orderJpaRepository.deleteById(orderId);
    }
}
