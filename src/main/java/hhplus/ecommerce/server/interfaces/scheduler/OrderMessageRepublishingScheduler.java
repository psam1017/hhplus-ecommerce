package hhplus.ecommerce.server.interfaces.scheduler;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;
import hhplus.ecommerce.server.domain.order.message.OrderMessageProducer;
import hhplus.ecommerce.server.domain.order.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderMessageRepublishingScheduler {

    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderMessageProducer orderMessageProducer;

    @Scheduled(fixedRate = 5000)
    public void republishOrderMessages() {
        List<OrderOutbox> outboxes = orderOutboxRepository.findAllByStatusAndCreatedDateTimeBefore(
                OrderOutboxStatus.CREATED,
                LocalDateTime.now().minusSeconds(10)
        );
        outboxes.forEach(outbox -> orderMessageProducer.sendMessage(outbox.getTopicName(), outbox.getTransactionKey(), outbox.getOriginalMessage()));
    }
}
