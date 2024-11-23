package hhplus.ecommerce.server.infrastructure.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.item.service.ItemRepository;
import hhplus.ecommerce.server.domain.order.repository.OrderItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
@Component
public class ItemCacheWarmer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final EntityManager em;
    private final ObjectMapper om;

    public void warmUpItemsPageCaches(int cacheLastPage) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(CacheName.ITEM_PAGE_WARM, "locked", Duration.ofMinutes(1));

        if (acquired != null && acquired) {
            log.trace("Warming up item caches");
            try {
                List<String> props = List.of("id", "price");
                List<String> dirs = List.of("asc", "desc");
                for (String prop : props) {
                    for (String dir : dirs) {
                        warmUpItemPageCache(cacheLastPage, prop, dir);
                    }
                    em.clear();
                }
                warmUpItemCountCache();
            } finally {
                redisTemplate.delete(CacheName.ITEM_PAGE_WARM);
            }
        }
    }

    public void warmUpTopItemCaches() {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(CacheName.ITEM_TOP_WARM, "locked", Duration.ofMinutes(1));
        LocalDateTime endDateTime = LocalDateTime.now();
        LocalDateTime startDateTime = endDateTime.minusDays(3);
        String cacheKey = "%s::%s".formatted(CacheName.ITEMS_TOP, LocalDate.now().toString());

        if (acquired != null && acquired) {
            log.trace("Warming up top item caches");
            try {
                List<Long> topItemIds = orderItemRepository.findTopItemIds(startDateTime, endDateTime);
                redisTemplate.opsForValue().set(cacheKey, topItemIds, Duration.ofHours(24));
            } finally {
                redisTemplate.delete(CacheName.ITEM_TOP_WARM);
            }
        }
    }

    public void refreshItemPageCaches(int cacheLastPage, Long hiddenItemId) {
        Optional<Item> optItem = itemRepository.findById(hiddenItemId);
        if (optItem.isPresent() && optItem.get().isActive()) {
            return;
        }

        List<String> props = List.of("id", "price");
        List<String> dirs = List.of("asc", "desc");
        AtomicBoolean found = new AtomicBoolean(false);
        for (String prop : props) {
            for (String dir : dirs) {
                int size = 10;

                for (int page = 1; page <= cacheLastPage; page++) {
                    String cacheKey = "%s::page:%d:size:%d:prop:%s:dir:%s".formatted(CacheName.ITEMS_PAGE, page, size, prop, dir);
                    Object cache = redisTemplate.opsForValue().get(cacheKey);
                    List<Item> items = converItems(cache);

                    if (items != null) {
                        for (Item item : items) {
                            if (Objects.equals(item.getId(), hiddenItemId)) {
                                warmUpItemPageCache(cacheLastPage, prop, dir);
                                found.set(true);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (found.get()) {
            warmUpItemCountCache();
        }
    }

    public void refershTopItemCaches(Long hiddenItemId) {
        Optional<Item> optItem = itemRepository.findById(hiddenItemId);
        if (optItem.isPresent() && optItem.get().isActive()) {
            return;
        }

        String cacheKey = "%s::%s".formatted(CacheName.ITEMS_TOP, LocalDate.now().toString());
        Object cache = redisTemplate.opsForValue().get(cacheKey);
        List<Long> topItemIds = converItemIds(cache);
        if (topItemIds != null) {
            topItemIds.stream().
                    filter(i -> Objects.equals(i, hiddenItemId))
                    .findAny()
                    .ifPresent(item -> warmUpTopItemCaches());
        }
    }

    @SuppressWarnings({"unchecked"})
    private void warmUpItemPageCache(int cacheLastPage, String prop, String dir) {
        int size = 10;
        int initialPage = 1;
        int initialOffset = 0;

        ItemCommand.ItemSearchCond searchCond = new ItemCommand.ItemSearchCond(initialPage, cacheLastPage * size, prop, dir, null);
        List<Item> items = itemRepository.findAllBySearchCond(searchCond);
        int itemSize = items.size();

        redisTemplate.execute(new SessionCallback<Void>() {
            public Void execute(@NotNull RedisOperations operations) throws DataAccessException {
                operations.multi();
                int page = initialPage;
                int offset = initialOffset;

                for (int i = 0; i < cacheLastPage; i++) {
                    if (offset >= itemSize) {
                        break;
                    }
                    int end = Math.min(offset + size, itemSize);
                    List<Item> itemsPage = items.subList(offset, end);
                    String cacheKey = "%s::page:%d:size:%d:prop:%s:dir:%s".formatted(CacheName.ITEMS_PAGE, page, size, prop, dir);
                    operations.opsForValue().set(cacheKey, itemsPage, Duration.ofMinutes(10));
                    offset += size;
                    page++;
                }
                operations.exec();
                return null;
            }
        });
    }

    private void warmUpItemCountCache() {
        long count = itemRepository.countAllBySearchCond(ItemCommand.ItemSearchCond.of(null, null, null, null, null));
        redisTemplate.opsForValue().set("%s::count".formatted(CacheName.ITEMS_PAGE), count, Duration.ofMinutes(10));
    }

    private List<Item> converItems(Object obj) {
        return obj == null ? null : om.convertValue(obj, new TypeReference<>() {});
    }

    private List<Long> converItemIds(Object obj) {
        return obj == null ? null : om.convertValue(obj, new TypeReference<>() {});
    }
}
