package hhplus.ecommerce.server.integration.infrastructure.event;

import hhplus.ecommerce.server.infrastructure.event.ItemModifyingEvent;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RecordApplicationEvents
public class ItemModifyingEventTest extends TestContainerEnvironment {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ApplicationEvents applicationEvents; // 주입 잘 되니까 걱정 X

    @DisplayName("상품 숨김 이벤트 발행 시 핸들러가 동작한다.")
    @Test
    void handle() {
        // given
        ItemModifyingEvent event = new ItemModifyingEvent(1L);

        // when
        applicationContext.publishEvent(event);

        // then
        List<ItemModifyingEvent> events = applicationEvents.stream(ItemModifyingEvent.class).toList();
        assertThat(events).isNotEmpty();
    }
}
