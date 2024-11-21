package hhplus.ecommerce.server.integration.interfaces.message.platform;

import hhplus.ecommerce.server.domain.order.OrderOutboxStatus;
import hhplus.ecommerce.server.domain.order.service.OrderService;
import hhplus.ecommerce.server.infrastructure.platform.OrderDataPlatform;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderOutboxJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.*;

public class OrderDataPlatformMessageConsumerTest extends TestContainerEnvironment {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderOutboxJpaRepository orderOutboxJpaRepository;

    @SpyBean
    OrderDataPlatform orderDataPlatform;

    @DisplayName("주문 완료 이벤트를 발행하고 데이터 플랫폼에서 해당 메시지를 소비할 수 있다.")
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

        // 리뷰포인트
        // 각 컨슈머가 소비하는 시점이 다를 수 있기에 위 코드만큼만 기다리면 비결정 테스트가 될 수 있습니다.
        // RepeatedTest 로 100번 정도 테스트했을 때 모두 성공하긴 했지만, 컴퓨터 성능에 영향을 받은 것으로 보입니다.
        // 메시지가 발행되는 것은 위 테스트만으로도 충분히 확인이 되고, OrderDataPlatformMessageConsumer 는 OrderDataPlatform 을 호출하기만 합니다.
        // 그런 관점에서, 이 테스트는 비결정 테스트 && 의미 없는 테스트 같은데 차라리 삭제하는 게 좋을까요?
        verify(orderDataPlatform, times(1)).saveOrderData(anyString());
    }
}
