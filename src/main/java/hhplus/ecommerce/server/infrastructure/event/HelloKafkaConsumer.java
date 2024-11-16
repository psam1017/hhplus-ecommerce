package hhplus.ecommerce.server.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

// 테스트 용으로 생성
@Slf4j
@RequiredArgsConstructor
@Service
public class HelloKafkaConsumer {

    @KafkaListener(topics = "hello-kafka", groupId = "ecommerce")
    public void consume(String message) {
        log.info("Consumed message: {}", message);
    }
}
