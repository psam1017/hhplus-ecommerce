package hhplus.ecommerce.server.interfaces.message.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.domain.order.message.OrderTopicName;
import hhplus.ecommerce.server.infrastructure.platform.OrderDataPlatform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderDataPlatformMessageConsumer {

    private final OrderDataPlatform orderDataPlatform;

    @KafkaListener(topics = OrderTopicName.ORDER_CREATED, groupId = "order")
    public void sendOrderDataToPlatform(ConsumerRecord<String, String> record) {
        orderDataPlatform.saveOrderData(record.value());
    }
}
