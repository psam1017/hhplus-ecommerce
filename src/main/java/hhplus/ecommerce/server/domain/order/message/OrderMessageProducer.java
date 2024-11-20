package hhplus.ecommerce.server.domain.order.message;

public interface OrderMessageProducer {

    void sendMessage(String topic, String key, String value);
}
