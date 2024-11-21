package hhplus.ecommerce.server.domain.order.repository;

import hhplus.ecommerce.server.domain.order.OrderOutbox;

import java.util.Optional;

public interface OrderOutboxRepository {

    OrderOutbox save(OrderOutbox orderOutbox);

    Optional<OrderOutbox> findByTransactionKey(String transactionKey);
}
