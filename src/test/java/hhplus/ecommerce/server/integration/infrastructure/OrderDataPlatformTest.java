package hhplus.ecommerce.server.integration.infrastructure;

import hhplus.ecommerce.server.infrastructure.data.OrderDataPlatform;
import hhplus.ecommerce.server.integration.TransactionalTestEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;

public class OrderDataPlatformTest extends TransactionalTestEnvironment {

    @Autowired
    OrderDataPlatform orderDataPlatform;

    @DisplayName("주문 데이터 전송 메서드를 예외 없이 실행할 수 있다.")
    @Test
    void saveOrderData() {
        // given
        Map<Long, Integer> itemIdItemAmountMap = Map.of(1L, 1);

        // when
        // then
        assertThatCode(() -> orderDataPlatform.saveOrderData(itemIdItemAmountMap))
                .doesNotThrowAnyException();
    }
}
