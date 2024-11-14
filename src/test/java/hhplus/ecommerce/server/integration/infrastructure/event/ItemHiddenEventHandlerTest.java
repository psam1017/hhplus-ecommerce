package hhplus.ecommerce.server.integration.infrastructure.event;

import hhplus.ecommerce.server.infrastructure.cache.ItemCacheWarmer;
import hhplus.ecommerce.server.infrastructure.event.ItemHiddenEvent;
import hhplus.ecommerce.server.infrastructure.event.ItemHiddenEventHandler;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RecordApplicationEvents
public class ItemHiddenEventHandlerTest extends TestContainerEnvironment {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ApplicationEvents applicationEvents; // 주입 잘 되니까 걱정 X

    @SpyBean
    ItemHiddenEventHandler itemHiddenEventHandler;

    @MockBean
    ItemCacheWarmer itemCacheWarmer;

    @DisplayName("상품 숨김 이벤트 발행 시 핸들러가 동작한다.")
    @Test
    void handle() {
        // given
        ItemHiddenEvent event = new ItemHiddenEvent(this, 1L);

        // when
        applicationContext.publishEvent(event);

        // then
        List<ItemHiddenEvent> events = applicationEvents.stream(ItemHiddenEvent.class).toList();
        assertThat(events).isNotEmpty();
    }
}
