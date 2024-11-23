package hhplus.ecommerce.server.infrastructure.repository.order;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutbox, Long> {


    Optional<OrderOutbox> findByTransactionKey(String transactionKey);

    List<OrderOutbox> findAllByStatusAndCreatedDateTimeBeforeAndRetryCountLessThan(OrderOutboxStatus status, LocalDateTime createdDateTime, int retryCount);
}
