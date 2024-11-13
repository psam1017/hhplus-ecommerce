package hhplus.ecommerce.server.infrastructure.event;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemRepository;
import hhplus.ecommerce.server.infrastructure.cache.ItemCacheWarmer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ItemModifyingEventHandler {

    private final ItemCacheWarmer itemCacheWarmer;

    @Async
    @EventListener(ItemModifyingEvent.class)
    public void refreshItemPageCaches(ItemModifyingEvent event) {
        itemCacheWarmer.refreshItemPageCaches(100, event.itemId());
    }

    @Async
    @EventListener(ItemModifyingEvent.class)
    public void refershTopItemCaches(ItemModifyingEvent event) {
        itemCacheWarmer.refershTopItemCaches(event.itemId());
    }
}
