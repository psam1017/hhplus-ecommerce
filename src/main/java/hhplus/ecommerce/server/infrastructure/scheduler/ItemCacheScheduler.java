package hhplus.ecommerce.server.infrastructure.scheduler;

import hhplus.ecommerce.server.infrastructure.cache.ItemCacheWarmer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ItemCacheScheduler {

    private final ItemCacheWarmer itemCacheWarmer;

    @Scheduled(cron = "0 0/5 * * * *")
    public void warmUp() {
        itemCacheWarmer.warmUpItemsPageCaches(100);
    }

    @Scheduled(cron = "1 0 0 * * *")
    public void warmUpTopItems() {
        itemCacheWarmer.warmUpTopItemCaches();
    }
}
