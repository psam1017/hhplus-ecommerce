package hhplus.ecommerce.server.config;

import hhplus.ecommerce.server.domain.order.message.OrderTopicName;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@Configuration
public class KafkaConfig {

    @Bean(name = OrderTopicName.ORDER_CREATED)
    public NewTopic orderCreatedTopic() {
        return TopicBuilder
                .name(OrderTopicName.ORDER_CREATED)
                .partitions(3)
                .build();
    }
}
