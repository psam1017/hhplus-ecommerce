package hhplus.ecommerce.server.integration.interfaces.message.order;

import hhplus.ecommerce.server.domain.order.OrderOutbox;
import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;
import hhplus.ecommerce.server.domain.order.service.OrderService;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderOutboxJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderMessageConsumerTest extends TestContainerEnvironment {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderOutboxJpaRepository orderOutboxJpaRepository;

    @DisplayName("주문 완료 이벤트를 발행하면 비동기적으로 발행된 메시지를 직접 소비하여 발행완료 상태로 바꿀 수 있다.")
    @Test
    void consumeOrderCreatedEvent() {
        // given
        Long orderId = 1L;
        Map<Long, Integer> itemIdStockAmountMap = Map.of(2L, 3);

        // when
        orderService.publishOrderCreatedEvent(orderId, itemIdStockAmountMap);

        // then
        Awaitility.await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> orderOutboxJpaRepository.findAll().get(0).getStatus() == OrderOutboxStatus.PUBLISHED);

        OrderOutbox orderOutbox = orderOutboxJpaRepository.findAll().get(0);
        assertThat(orderOutbox.getStatus()).isEqualTo(OrderOutboxStatus.PUBLISHED);
        assertThat(orderOutbox.getPublishedDateTime()).isNotNull();
    }
}
