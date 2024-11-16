package hhplus.ecommerce.server.infrastructure.event;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// 테스트 용으로 생성
@Slf4j
@RequiredArgsConstructor
@Service
public class HelloKafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage() {
        kafkaTemplate.send("hello-kafka", "kafka");
    }

    @PostConstruct
    public void init() {
        // 실행 이후 시험용으로 메시지를 발행
        sendMessage();
    }
}
