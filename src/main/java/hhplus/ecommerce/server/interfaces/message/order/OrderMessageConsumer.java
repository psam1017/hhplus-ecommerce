package hhplus.ecommerce.server.interfaces.message.order;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.message.OrderTopicName;
import hhplus.ecommerce.server.domain.order.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderMessageConsumer {

    private final OrderOutboxRepository orderOutboxRepository;

    @KafkaListener(topics = OrderTopicName.ORDER_CREATED, groupId = "ecommerce")
    public void listenOrderCreated(ConsumerRecord<String, String> record) {
        String key = record.key();
        Optional<OrderOutbox> optOutbox = orderOutboxRepository.findByTransactionKey(key);
        if (optOutbox.isPresent()) {
            OrderOutbox outbox = optOutbox.get();
            outbox.logPublished();
        } else {
            log.warn("OrderOutbox not found for transactionKey: {}", key);
        }
    }
}
