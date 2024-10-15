package hhplus.ecommerce.server.infrastructure.order;

import hhplus.ecommerce.server.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    List<Order> findAllByUserId(Long userId);
}
