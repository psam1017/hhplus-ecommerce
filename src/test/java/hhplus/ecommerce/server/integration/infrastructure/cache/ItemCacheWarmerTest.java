package hhplus.ecommerce.server.integration.infrastructure.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStatus;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.item.service.ItemRepository;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.infrastructure.cache.CacheName;
import hhplus.ecommerce.server.infrastructure.cache.ItemCacheWarmer;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemJpaCommandRepository;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemStockJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

public class ItemCacheWarmerTest extends TestContainerEnvironment {

    @Autowired
    ItemCacheWarmer itemCacheWarmer;

    @Autowired
    ItemService itemService;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @SpyBean
    ItemRepository itemRepository;

    @Autowired
    ItemJpaCommandRepository itemJpaCommandRepository;

    @Autowired
    ItemStockJpaRepository itemStockJpaRepository;

    @Autowired
    OrderJpaRepository orderJpaRepository;

    @Autowired
    OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    ObjectMapper om;

    private static Stream<Arguments> provideWarmUpItemsPageCachesData() {
        return Stream.of(
                Arguments.of(90, 9),
                Arguments.of(91, 10),
                Arguments.of(99, 10),
                Arguments.of(100, 10),
                Arguments.of(101, 10)
        );
    }

    @MethodSource("provideWarmUpItemsPageCachesData")
    @DisplayName("상품 목록을 최대 페이지 수 이내에서 정렬속성과 방향 별로 캐싱할 수 있다.")
    @ParameterizedTest
    void warmUpItemsPageCaches(int itemSize, int expectedPageCount) {
        // given
        int cacheLastPage = 10;
        int pageSize = 10;
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < itemSize; i++) {
            items.add(buildItem("item" + i, i));
        }
        itemJpaCommandRepository.saveAll(items);

        // when
        itemCacheWarmer.warmUpItemsPageCaches(cacheLastPage);

        // then
        verify(itemRepository, times(4)).findAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
        verify(itemRepository, times(1)).countAllBySearchCond(any(ItemCommand.ItemSearchCond.class));

        List<String> props = List.of("id", "price");
        List<String> dirs = List.of("asc", "desc");
        Set<String> cacheKeys = redisTemplate.keys("*");
        assertThat(cacheKeys).isNotNull();
        assertThat(cacheKeys.size()).isEqualTo(props.size() * dirs.size() * expectedPageCount + 1);

        for (String prop : props) {
            for (String dir : dirs) {
                for (int page = 1; page <= expectedPageCount; page++) {
                    String cacheKey = "%s::page:%d:size:%d:prop:%s:dir:%s".formatted(CacheName.ITEMS_PAGE, page, pageSize, prop, dir);
                    Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
                    List<Item> itemsAfterCache = convertItems(dataAfterCache);
                    assertThat(itemsAfterCache).isNotEmpty();
                }
            }
        }
    }

    @DisplayName("상품 목록을 미리 캐싱해두면 상품을 조회할 때 쿼리를 수행하지 않는다.")
    @Test
    void whenWarmUpItemsPageCaches_thenNoQuery() {
        // given
        createItem("Test Item1", 1000);
        itemCacheWarmer.warmUpItemsPageCaches(1);
        ItemCommand.ItemSearchCond searchCond = new ItemCommand.ItemSearchCond(1, 10, "id", "asc", null);

        // 캐시 워밍을 위한 쿼리가 발생함
        verify(itemRepository, times(4)).findAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
        verify(itemRepository, times(1)).countAllBySearchCond(any(ItemCommand.ItemSearchCond.class));

        // when
        itemService.findItemsBySearchCond(searchCond);
        itemService.countItemsBySearchCond(searchCond, 10);

        // then - 추가적인 쿼리가 발생하지 않음
        verify(itemRepository, times(4)).findAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
        verify(itemRepository, times(1)).countAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
    }

    @DisplayName("상위 상품 목록을 미리 캐싱할 수 있다.")
    @Test
    void warmUpTopItemCaches() {
        // given
        Item item1 = createItem("Test Item1", 1000);
        createItemStock(item1);
        Order order = createOrder();
        createOrderItem(item1, order);

        String cacheKey = "%s::%s".formatted(CacheName.ITEMS_TOP, LocalDate.now().toString());
        Object dataBeforeCache = redisTemplate.opsForValue().get(cacheKey);

        // when
        itemCacheWarmer.warmUpTopItemCaches();

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
        assertThat(dataBeforeCache).isNull();
        assertThat(dataAfterCache).isNotNull();
        List<Item> itemsAfterCache = convertItems(dataAfterCache);
        assertThat(itemsAfterCache).isNotEmpty();

        verify(itemRepository, times(1)).findTopItems(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @DisplayName("상위 상품 목록을 미리 캐싱해두면 상품을 조회할 때 쿼리를 수행하지 않는다.")
    @Test
    void whenWarmUpTopItemCaches_thenNoQuery() {
        // given
        Item item1 = createItem("Test Item1", 1000);
        createItemStock(item1);
        Order order = createOrder();
        createOrderItem(item1, order);
        itemCacheWarmer.warmUpTopItemCaches();

        // 캐시 워밍을 위한 쿼리가 발생함
        verify(itemRepository, times(1)).findTopItems(any(LocalDateTime.class), any(LocalDateTime.class));

        // when
        itemService.findTopItems();

        // then - 추가적인 쿼리가 발생하지 않음
        verify(itemRepository, times(1)).findTopItems(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @DisplayName("상위 상품 목록에서 숨겨야 할 상품이 있다면, 캐시를 갱신한다.")
    @Test
    void whenHiddenItemExists_thenRefreshTopItemCaches() {
        // given
        Order order = createOrder();
        Item item1 = createItem("Test Item1", 1000);
        Item item2 = createItem("Test Item2", 2000);
        createItemStock(item1);
        createItemStock(item2);
        createOrderItem(item1, order);
        createOrderItem(item2, order);

        itemCacheWarmer.warmUpTopItemCaches();
        String cacheKey = "%s::%s".formatted(CacheName.ITEMS_TOP, LocalDate.now().toString());
        Object cacheBeforeRefresh = redisTemplate.opsForValue().get(cacheKey);
        List<Item> topItemsBeforeRefresh = convertItems(cacheBeforeRefresh);

        itemRepository.modifyItemStatus(item2.getId(), ItemStatus.HIDDEN);

        // when
        itemCacheWarmer.refershTopItemCaches(item2.getId());

        // then
        Object cacheAfterRefresh = redisTemplate.opsForValue().get(cacheKey);
        List<Item> topItemsAfterRefresh = convertItems(cacheAfterRefresh);

        assertThat(topItemsBeforeRefresh).hasSize(2)
                .extracting(Item::getId)
                .containsExactly(
                        item2.getId(),
                        item1.getId()
                );
        assertThat(topItemsAfterRefresh).hasSize(1)
                .extracting(Item::getId)
                .containsExactly(item1.getId());
    }

    @DisplayName("상위 상품 목록에서 숨겨야 할 상품이 포함되지 않는다면 캐시를 갱신하지 않는다.")
    @Test
    void whenHiddenItemNotExists_thenDoNotRefreshTopItemCaches() {
        // given
        Order order = createOrder();
        Item item1 = createItem("Test Item1", 1000);
        createItemStock(item1);
        createOrderItem(item1, order);

        itemCacheWarmer.warmUpTopItemCaches();

        // 캐시 워밍을 위한 쿼리가 발생함
        verify(itemRepository, times(1)).findTopItems(any(LocalDateTime.class), any(LocalDateTime.class));

        // when
        itemCacheWarmer.refershTopItemCaches(item1.getId() + 1);

        // then - 추가적인 쿼리가 발생하지 않음
        verify(itemRepository, times(1)).findTopItems(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @DisplayName("상품 목록 캐시에서 숨겨야 할 상품이 있다면, 캐시를 갱신한다.")
    @Test
    void whenHiddenItemExists_thenRefreshItemsPageCaches() {
        // given
        Item item1 = createItem("Test Item1", 1000);
        Item item2 = createItem("Test Item2", 2000);
        createItemStock(item1);
        createItemStock(item2);

        itemCacheWarmer.warmUpItemsPageCaches(1);
        itemRepository.modifyItemStatus(item2.getId(), ItemStatus.HIDDEN);

        // when
        itemCacheWarmer.refreshItemPageCaches(1, item2.getId());

        // then
        verify(itemRepository, times(8)).findAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
        verify(itemRepository, times(2)).countAllBySearchCond(any(ItemCommand.ItemSearchCond.class));

        List<String> props = List.of("id", "price");
        List<String> dirs = List.of("asc", "desc");
        for (String prop : props) {
            for (String dir : dirs) {
                String cacheKey = "%s::page:%d:size:%d:prop:%s:dir:%s".formatted(CacheName.ITEMS_PAGE, 1, 10, prop, dir);
                Object cacheAfterRefresh = redisTemplate.opsForValue().get(cacheKey);
                List<Item> itemsAfterRefresh = convertItems(cacheAfterRefresh);
                assertThat(itemsAfterRefresh).hasSize(1)
                        .extracting(Item::getId)
                        .containsExactly(item1.getId());
            }
        }
    }

    @DisplayName("상품 목록 캐시에서 숨겨야 할 상품이 포함되지 않는다면 캐시를 갱신하지 않는다.")
    @Test
    void whenHiddenItemNotExists_thenDoNotRefreshItemsPageCaches() {
        // given
        Item item1 = createItem("Test Item1", 1000);
        createItemStock(item1);

        itemCacheWarmer.warmUpItemsPageCaches(1);

        // 캐시 워밍을 위한 쿼리가 발생함
        verify(itemRepository, times(4)).findAllBySearchCond(any(ItemCommand.ItemSearchCond.class));

        // when
        itemCacheWarmer.refreshItemPageCaches(1, item1.getId() + 1);

        // then - 추가적인 쿼리가 발생하지 않음
        verify(itemRepository, times(4)).findAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
    }

    private Item buildItem(String name, int price) {
        return Item.builder()
                .name(name)
                .price(price)
                .build();
    }

    private Item createItem(String name, int price) {
        return itemJpaCommandRepository.save(Item.builder()
                .name(name)
                .price(price)
                .build());
    }

    private void createItemStock(Item item) {
        itemStockJpaRepository.save(ItemStock.builder()
                .amount(10)
                .item(item)
                .build());
    }

    private Order createOrder() {
        Order order = Order.builder()
                .status(OrderStatus.ORDERED)
                .orderDateTime(LocalDateTime.now().minusDays(1))
                .build();
        orderJpaRepository.save(order);
        return order;
    }

    private void createOrderItem(Item item1, Order order) {
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item1)
                .name(item1.getName())
                .price(item1.getPrice())
                .quantity(1)
                .build();
        orderItemJpaRepository.save(orderItem);
    }

    private List<Item> convertItems(Object obj) {
        return om.convertValue(obj, new TypeReference<>() {});
    }
}
