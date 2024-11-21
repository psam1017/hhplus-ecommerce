package hhplus.ecommerce.server.integration.infrastructure.message;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;
import hhplus.ecommerce.server.domain.order.message.OrderTopicName;
import hhplus.ecommerce.server.infrastructure.message.OrderMessageKafkaProducer;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderOutboxJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class OrderMessageKafkaProducerTest extends TestContainerEnvironment {

    @Autowired
    OrderMessageKafkaProducer orderMessageKafkaProducer;

    @Autowired
    OrderOutboxJpaRepository orderOutboxJpaRepository;

    @DisplayName("발행에 실패한 메시지를 재전송할 수 있다.")
    @Test
    void sendFailedMessages() {
        // given
        createOutbox(OrderTopicName.ORDER_CREATED, "key", "value");

        // when
        orderMessageKafkaProducer.sendFailedMessages(
                LocalDateTime.now().plusSeconds(10),
                3
        );

        // then
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> orderOutboxJpaRepository.findAll().get(0).getStatus() == OrderOutboxStatus.PUBLISHED);
    }

    @DisplayName("재발행에 실패하면 재발행 횟수를 증가시킬 수 있다.")
    @Test
    void sendFailedMessagesAndFailAgain() {
        // given
        createOutbox(null, null, null);

        // when
        orderMessageKafkaProducer.sendFailedMessages(
                LocalDateTime.now().plusSeconds(10),
                3
        );

        // then
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> orderOutboxJpaRepository.findAll().get(0).getRetryCount() == 1);
    }

    private void createOutbox(String topic, String key, String value) {
        orderOutboxJpaRepository.save(OrderOutbox.builder()
                .topicName(topic)
                .transactionKey(key)
                .originalMessage(value)
                .build());
    }
}
