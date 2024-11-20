package hhplus.ecommerce.server.domain.order.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;
import hhplus.ecommerce.server.domain.order.repository.OrderOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static hhplus.ecommerce.server.config.AsyncConfig.EVENT_ASYNC_TASK_EXECUTOR;

@RequiredArgsConstructor
@Component
public class OrderEventListener {

    private final OrderOutboxRepository orderOutboxRepository;
    private final OrderMessageProducer orderMessageProducer;
    private final ObjectMapper om;

    @TransactionalEventListener(
            value = OrderCreatedEvent.class,
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void saveOrderOutbox(OrderCreatedEvent event) throws JsonProcessingException {
        orderOutboxRepository.save(OrderOutbox.builder()
                        .topicName(OrderTopicName.ORDER_CREATED)
                        .transactionKey(event.transactionKey())
                        .originalMessage(writeMessage(event))
                        .build());
    }

    @Async(EVENT_ASYNC_TASK_EXECUTOR)
    @TransactionalEventListener(
            value = OrderCreatedEvent.class,
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void sendOrderCreatedMessage(OrderCreatedEvent event) {
        try {
            orderMessageProducer.sendMessage(OrderTopicName.ORDER_CREATED, event.transactionKey(), writeMessage(event));
        } catch (Exception e) {
            orderOutboxRepository.updateMessageStatusByTransactionKey(event.transactionKey(), OrderOutboxStatus.FAILED, e.getMessage());
        }
    }

    private String writeMessage(OrderCreatedEvent event) throws JsonProcessingException {
        return om.writeValueAsString(event);
    }
}
