package hhplus.ecommerce.server.infrastructure.repository.order;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;
import hhplus.ecommerce.server.domain.order.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Repository
public class OrderOutboxRepositoryImpl implements OrderOutboxRepository {

    private final OrderOutboxJpaRepository orderOutboxJpaRepository;

    @Override
    public OrderOutbox save(OrderOutbox orderOutbox) {
        return orderOutboxJpaRepository.save(orderOutbox);
    }

    @Override
    public Optional<OrderOutbox> findByTransactionKey(String transactionKey) {
        return orderOutboxJpaRepository.findByTransactionKey(transactionKey);
    }

    @Override
    public List<OrderOutbox> findAllByStatusAndCreatedDateTimeBefore(OrderOutboxStatus status, LocalDateTime createdDateTime) {
        return orderOutboxJpaRepository.findAllByStatusAndCreatedDateTimeBefore(status, createdDateTime);
    }
}
