package hhplus.ecommerce.server.infrastructure.order;

import hhplus.ecommerce.server.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
}
