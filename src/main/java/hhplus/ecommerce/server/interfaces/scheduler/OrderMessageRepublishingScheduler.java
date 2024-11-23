package hhplus.ecommerce.server.interfaces.scheduler;

import hhplus.ecommerce.server.domain.order.message.OrderMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderMessageRepublishingScheduler {

    private final OrderMessageProducer orderMessageProducer;

    @Scheduled(fixedRate = 5000)
    public void republishOrderMessages() {
        orderMessageProducer.sendFailedMessages(LocalDateTime.now().minusSeconds(10), 3);
    }
}
