package hhplus.ecommerce.server.infrastructure.repository.order;

import hhplus.ecommerce.server.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findAllByOrderId(Long orderId);

    @Query("""
            select i.order.id, sum(i.price * i.quantity)
            from OrderItem i
            where i.order.id in :orderIds
            group by i.order.id
            """)
    List<Object[]> findOrderAmounts(List<Long> orderIds);
}
