package hhplus.ecommerce.server.integration.infrastructure.cache;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.item.service.ItemRepository;
import hhplus.ecommerce.server.infrastructure.cache.ItemCacheWarmer;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemJpaCommandRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;

public class ItemCacheWarmerConcurrencyTest extends TestContainerEnvironment {

    @Autowired
    ItemCacheWarmer itemCacheWarmer;

    @SpyBean
    ItemRepository itemRepository;

    @Autowired
    ItemJpaCommandRepository itemJpaCommandRepository;

    @DisplayName("한 번에 여러 번의 캐시 워밍을 요청해도 한 번만 실행되어야 한다.")
    @Test
    void warmUpItemsPageCachesOnlyOnce() throws InterruptedException {
        // given
        for (int i = 0; i < 11; i++) {
            createItem();
        }

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        for (int i = 0; i < 2; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    itemCacheWarmer.warmUpItemsPageCaches(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // when
        startLatch.countDown();
        endLatch.await();

        // then
        verify(itemRepository, times(4)).findAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
        verify(itemRepository, times(1)).countAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
    }

    private void createItem() {
        itemJpaCommandRepository.save(Item.builder()
                .name("item")
                .price(1000)
                .build());
    }
}
