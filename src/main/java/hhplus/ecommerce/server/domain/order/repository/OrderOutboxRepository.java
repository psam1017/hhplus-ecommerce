package hhplus.ecommerce.server.domain.order.repository;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;

import java.util.Optional;

public interface OrderOutboxRepository {

    OrderOutbox save(OrderOutbox orderOutbox);

    Optional<OrderOutbox> findByTransactionKey(String transactionKey);

    void updateMessageStatusByTransactionKey(String transactionKey, OrderOutboxStatus status, String reason);
}
