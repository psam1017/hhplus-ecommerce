package hhplus.ecommerce.server.infrastructure.repository.order;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderOutboxJpaRepository extends JpaRepository<OrderOutbox, Long> {


    Optional<OrderOutbox> findByTransactionKey(String transactionKey);
}
