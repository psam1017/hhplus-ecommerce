package hhplus.ecommerce.server.infrastructure.event;

import hhplus.ecommerce.server.infrastructure.cache.ItemCacheWarmer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ItemHiddenEventHandler {

    private final ItemCacheWarmer itemCacheWarmer;

    @Async
    @EventListener(ItemHiddenEvent.class)
    public void handle(ItemHiddenEvent event) {
        itemCacheWarmer.refreshItemPageCaches(100, event.getItemId());
        itemCacheWarmer.refershTopItemCaches(event.getItemId());
    }
}
