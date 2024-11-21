package hhplus.ecommerce.server.infrastructure.message;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;
import hhplus.ecommerce.server.domain.order.message.OrderMessageProducer;
import hhplus.ecommerce.server.domain.order.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderMessageKafkaProducer implements OrderMessageProducer {

    private final OrderOutboxRepository orderOutboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void sendMessage(String topic, String key, String value) {
        kafkaTemplate.send(topic, key, value);
    }

    @Transactional
    @Override
    public void sendFailedMessages(LocalDateTime createdDateTime, int retryCount) {
        List<OrderOutbox> outboxes = orderOutboxRepository.findAllByStatusAndCreatedDateTimeBeforeAndRetryCountLessThan(
                OrderOutboxStatus.CREATED,
                createdDateTime,
                retryCount
        );
        for (OrderOutbox outbox : outboxes) {
            try {
                kafkaTemplate.send(outbox.getTopicName(), outbox.getTransactionKey(), outbox.getOriginalMessage());
            } catch (Exception e) {
                outbox.logFailed(e.getMessage());
                log.error("Failed to republish order message: {}", outbox.getTransactionKey());
            }
        }
    }
}
