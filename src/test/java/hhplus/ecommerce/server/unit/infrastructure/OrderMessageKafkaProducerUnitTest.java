package hhplus.ecommerce.server.unit.infrastructure;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;
import hhplus.ecommerce.server.domain.order.repository.OrderOutboxRepository;
import hhplus.ecommerce.server.infrastructure.message.OrderMessageKafkaProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderMessageKafkaProducerUnitTest {

    @InjectMocks
    OrderMessageKafkaProducer orderMessageKafkaProducer;

    @Mock
    OrderOutboxRepository orderOutboxRepository;

    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    @DisplayName("발행에 실패한 메시지를 재전송할 수 있다.")
    @Test
    void sendFailedMessages() {
        // given
        OrderOutbox outbox = buildOutbox("topic", "key", "value");
        when(orderOutboxRepository.findAllByStatusAndCreatedDateTimeBeforeAndRetryCountLessThan(
                any(OrderOutboxStatus.class),
                any(LocalDateTime.class),
                anyInt()
        )).thenReturn(List.of(outbox));

        // when
        orderMessageKafkaProducer.sendFailedMessages(LocalDateTime.now().minusSeconds(10), 3);

        // then
        verify(orderOutboxRepository, times(1)).findAllByStatusAndCreatedDateTimeBeforeAndRetryCountLessThan(
                any(OrderOutboxStatus.class),
                any(LocalDateTime.class),
                anyInt());
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), anyString());
    }

    @DisplayName("재발행에 실패하면 재발행 횟수를 증가시킬 수 있다.")
    @Test
    void sendFailedMessagesAndFailAgain() {
        // given
        OrderOutbox outbox = buildOutbox("topic", "key", "value");
        when(orderOutboxRepository.findAllByStatusAndCreatedDateTimeBeforeAndRetryCountLessThan(
                any(OrderOutboxStatus.class),
                any(LocalDateTime.class),
                anyInt()
        )).thenReturn(List.of(outbox));
        willThrow(new RuntimeException("메시지 발행 실패"))
                .given(kafkaTemplate).send(anyString(), anyString(), anyString());

        // when
        orderMessageKafkaProducer.sendFailedMessages(LocalDateTime.now().minusSeconds(10), 3);

        // then
        assertThat(outbox.getRetryCount()).isEqualTo(1);
    }

    private static OrderOutbox buildOutbox(String topic, String key, String value) {
        return OrderOutbox.builder()
                .topicName(topic)
                .transactionKey(key)
                .originalMessage(value)
                .build();
    }
}
