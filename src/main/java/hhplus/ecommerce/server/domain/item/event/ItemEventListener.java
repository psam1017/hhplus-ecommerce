package hhplus.ecommerce.server.domain.item.event;

import hhplus.ecommerce.server.infrastructure.cache.ItemCacheWarmer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ItemEventListener {

    private final ItemCacheWarmer itemCacheWarmer;

    @Async
    @EventListener(ItemModifiedEvent.class)
    public void refreshItemPageCaches(ItemModifiedEvent event) {
        itemCacheWarmer.refreshItemPageCaches(100, event.itemId());
    }

    @Async
    @EventListener(ItemModifiedEvent.class)
    public void refershTopItemCaches(ItemModifiedEvent event) {
        itemCacheWarmer.refershTopItemCaches(event.itemId());
    }
}
