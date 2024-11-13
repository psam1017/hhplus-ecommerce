package hhplus.ecommerce.server.infrastructure.repository.order;

import hhplus.ecommerce.server.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    @Query("""
            SELECT oi.item.id
              FROM OrderItem oi
             INNER JOIN Order o
                ON oi.order.id = o.id
             WHERE o.status = 'ORDERED'
               AND o.orderDateTime BETWEEN :startDateTime AND :endDateTime
             GROUP BY oi.item.id
             ORDER BY SUM(oi.totalAmount) DESC
             LIMIT 10
            """)
    List<Long> findTopItemIds(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
