package hhplus.ecommerce.server.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean(name = "helloKafkaTopic")
    public NewTopic helloKafkaTopic() {
        return TopicBuilder
                .name("hello-kafka")
                .partitions(3)
                .build();
    }
}
