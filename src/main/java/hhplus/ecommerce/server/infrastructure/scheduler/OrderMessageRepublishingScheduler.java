package hhplus.ecommerce.server.infrastructure.scheduler;

import hhplus.ecommerce.server.domain.order.message.OrderMessageProducer;
import hhplus.ecommerce.server.domain.order.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderMessageRepublishingScheduler {

    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderMessageProducer orderMessageProducer;
}
