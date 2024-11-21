package hhplus.ecommerce.server.domain.order.message;

import java.time.LocalDateTime;

public interface OrderMessageProducer {

    void sendMessage(String topic, String key, String value);

    void sendFailedMessages(LocalDateTime createdDateTime, int retryCount);
}
