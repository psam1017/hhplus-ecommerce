package hhplus.ecommerce.server.infrastructure.item;

import hhplus.ecommerce.server.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {

    @Query("""
            select i
            from Item i
            left join OrderItem oi
            on i.id = oi.item.id
            left join Order o
            on oi.order.id = o.id
            where o.status = 'ORDERED'
              and o.orderDateTime between :startDateTime and :endDateTime
            group by i
            order by sum(oi.quantity * oi.price) desc
            limit 5
            """)
    List<Item> findTopItemsOrderDateTimeBetween(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
