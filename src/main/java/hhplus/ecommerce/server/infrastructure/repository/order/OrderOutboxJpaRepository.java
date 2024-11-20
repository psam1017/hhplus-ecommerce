package hhplus.ecommerce.server.infrastructure.repository.order;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutbox, Long> {


    Optional<OrderOutbox> findByTransactionKey(String transactionKey);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE OrderOutbox o
            SET o.status = :status,
                o.reason = :reason
            WHERE o.transactionKey = :transactionKey
            """)
    void updateMessageStatusByTransactionKey(
            String transactionKey,
            OrderOutboxStatus status,
            String reason
    );
}
