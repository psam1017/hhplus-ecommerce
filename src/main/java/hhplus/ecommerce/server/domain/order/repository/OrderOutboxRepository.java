package hhplus.ecommerce.server.domain.order.repository;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderOutboxRepository {

    OrderOutbox save(OrderOutbox orderOutbox);

    Optional<OrderOutbox> findByTransactionKey(String transactionKey);

    List<OrderOutbox> findAllByStatusAndCreatedDateTimeBefore(OrderOutboxStatus status, LocalDateTime createdDateTime);
}
