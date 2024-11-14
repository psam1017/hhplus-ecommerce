package hhplus.ecommerce.server.integration.domain.item;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.item.service.ItemRepository;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.infrastructure.cache.CacheName;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemJpaCommandRepository;
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
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

public class ItemServiceCacheTest extends TestContainerEnvironment {

    @Autowired
    ItemService itemService;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @SpyBean
    ItemRepository itemRepository;

    @Autowired
    ItemJpaCommandRepository itemJpaCommandRepository;

    @Autowired
    OrderJpaRepository orderJpaRepository;

    @Autowired
    OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    ObjectMapper om;

    @DisplayName("상위 상품 목록을 조회하면서 cache miss 가 발생하면 Look Around 로 캐시를 생성한다.")
    @Test
    void findTopItemsWithCacheMiss() {
        //  given
        String cacheKey = "%s::%s".formatted(CacheName.ITEMS_TOP, LocalDate.now().toString());
        Object dataBeforeCache = redisTemplate.opsForValue().get(cacheKey);

        Item item1 = createItem("Test Item1", 1000);
        Item item2 = createItem("Test Item2", 2000);

        Order order = createOrder();
        createOrderItem(item1, order, 10);

        // when
        List<Item> result = itemService.findTopItems();

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
        List<Item> cachedItems = convertItems(dataAfterCache);
        Item cachedItem = cachedItems.get(0);

        assertThat(dataBeforeCache).isNull();
        assertThat(dataAfterCache).isNotNull();
        assertThat(result).hasSize(1)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(tuple(item1.getId(), item1.getName(), item1.getPrice()))
                .containsExactly(tuple(cachedItem.getId(), cachedItem.getName(), cachedItem.getPrice()))
                .doesNotContain(tuple(item2.getId(), item2.getName(), item2.getPrice()));
    }

    @DisplayName("상위 상품 목록에서 cache hit 이 발생하면, 쿼리는 실행되지 않는다.")
    @Test
    void findTopItemsWithCacheHit() {
        // given
        Item item1 = createItem("Test Item1", 1000);
        Item item2 = createItem("Test Item2", 2000);

        Order order = createOrder();
        createOrderItem(item1, order, 10);
        createOrderItem(item2, order, 4);

        // when
        itemService.findTopItems();
        itemService.findTopItems();

        // then
        verify(itemRepository, times(1)).findTopItems(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @DisplayName("페이징할 때 검색어가 없으면 캐시 데이터를 생성한다.")
    @Test
    void pageItemsWithCacheMiss() {
        // given
        int page = 1;
        int size = 10;
        String prop = "id";
        String dir = "desc";
        String cacheKey = "%s::page:%d:size:%d:prop:%s:dir:%s".formatted(CacheName.ITEMS_PAGE, page, size, prop, dir);
        Object dataBeforeCache = redisTemplate.opsForValue().get(cacheKey);

        Item item1 = createItem("Test Item1", 1000);
        Item item2 = createItem("Test Item2", 2000);

        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(page, size, prop, dir, null);

        // when
        List<Item> items = itemService.findItemsBySearchCond(searchCond);

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
        List<Item> cachedItems = convertItems(dataAfterCache);
        Item cachedItem1 = cachedItems.get(0);
        Item cachedItem2 = cachedItems.get(1);

        assertThat(dataBeforeCache).isNull();
        assertThat(dataAfterCache).isNotNull();
        assertThat(items).hasSize(2)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(
                        tuple(item2.getId(), item2.getName(), item2.getPrice()),
                        tuple(item1.getId(), item1.getName(), item1.getPrice())
                )
                .containsExactly(
                        tuple(cachedItem1.getId(), cachedItem1.getName(), cachedItem1.getPrice()),
                        tuple(cachedItem2.getId(), cachedItem2.getName(), cachedItem2.getPrice())
                );
    }

    @DisplayName("페이징할 때 검색어가 있으면 캐시 데이터를 생성하지 않는다.")
    @Test
    void pageItemsWithCacheHit() {
        // given
        int page = 1;
        int size = 10;
        String prop = "id";
        String dir = "desc";
        String cacheKey = "%s::page:%d:size:%d:prop:%s:dir:%s".formatted(CacheName.ITEMS_PAGE, page, size, prop, dir);
        Object dataBeforeCache = redisTemplate.opsForValue().get(cacheKey);

        createItem("Test Item1", 1000);
        createItem("Test Item2", 2000);

        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(page, size, prop, dir, "Test");

        // when
        itemService.findItemsBySearchCond(searchCond);

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);

        assertThat(dataBeforeCache).isNull();
        assertThat(dataAfterCache).isNull();
    }

    @DisplayName("페이지 수가 100 을 넘으면 캐시 데이터를 생성하지 않는다.")
    @Test
    void pageItemsWithCacheMissOver100() {
        // given
        int page = 101;
        int size = 10;
        String prop = "id";
        String dir = "desc";
        String cacheKey = "%s::page:%d:size:%d:prop:%s:dir:%s".formatted(CacheName.ITEMS_PAGE, page, size, prop, dir);

        createItem("Test Item1", 1000);
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(page, size, prop, dir, null);

        // when
        itemService.findItemsBySearchCond(searchCond);

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
        assertThat(dataAfterCache).isNull();
    }

    private static Stream<Arguments> provideSizes() {
        return Stream.of(
                Arguments.of(9, false),
                Arguments.of(10, true),
                Arguments.of(11, false)
        );
    }

    @MethodSource("provideSizes")
    @DisplayName("size 는 10이 아니면 캐시 데이터를 생성하지 않는다.")
    @ParameterizedTest
    void pageItemsWithInvalidSize(int size, boolean expected) {
        // given
        int page = 1;
        String prop = "id";
        String dir = "desc";
        String cacheKey = "%s::page:%d:size:%d:prop:%s:dir:%s".formatted(CacheName.ITEMS_PAGE, page, size, prop, dir);

        createItem("Test Item1", 1000);
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(page, size, prop, dir, null);

        // when
        itemService.findItemsBySearchCond(searchCond);

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
        assertThat(dataAfterCache != null).isEqualTo(expected);
    }

    private static Stream<Arguments> provideProps() {
        return Stream.of(
                Arguments.of("id", true),
                Arguments.of("price", true),
                Arguments.of("name", false)
        );
    }

    @MethodSource("provideProps")
    @DisplayName("정렬 속성은 id, price 만 허용된다.")
    @ParameterizedTest
    void pageItemsWithInvalidProp(String prop, boolean expected) {
        // given
        int page = 1;
        int size = 10;
        String dir = "desc";
        String cacheKey = "%s::page:%d:size:%d:prop:%s:dir:%s".formatted(CacheName.ITEMS_PAGE, page, size, prop, dir);

        createItem("Test Item1", 1000);
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(page, size, prop, dir, null);

        // when
        itemService.findItemsBySearchCond(searchCond);

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
        assertThat(dataAfterCache != null).isEqualTo(expected);
    }

    private static Stream<Arguments> provideDirs() {
        return Stream.of(
                Arguments.of("asc", true),
                Arguments.of("desc", true),
                Arguments.of("invalid", false)
        );
    }

    @MethodSource("provideDirs")
    @DisplayName("정렬 방향은 asc, desc 만 허용된다.")
    @ParameterizedTest
    void pageItemsWithInvalidDir(String dir, boolean expected) {
        // given
        int page = 1;
        int size = 10;
        String prop = "id";
        String cacheKey = "%s::page:%d:size:%d:prop:%s:dir:%s".formatted(CacheName.ITEMS_PAGE, page, size, prop, dir);

        createItem("Test Item1", 1000);
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(page, size, prop, dir, null);

        // when
        itemService.findItemsBySearchCond(searchCond);

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
        assertThat(dataAfterCache != null).isEqualTo(expected);
    }

    @DisplayName("상품 개수를 조회하면서 cache miss 가 발생하면 Look Around 로 캐시를 생성한다.")
    @Test
    void countItemsBySearchCondWithCacheMiss() {
        // given
        String cacheKey = "%s::count".formatted(CacheName.ITEMS_PAGE);
        Object dataBeforeCache = redisTemplate.opsForValue().get(cacheKey);

        createItem("Test Item1", 1000);

        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(1, 10, "id", "desc", null);

        // when
        long result = itemService.countItemsBySearchCond(searchCond, 1);

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
        assertThat(dataBeforeCache).isNull();
        assertThat(dataAfterCache).isNotNull();
        assertThat(result).isEqualTo(Long.parseLong(dataAfterCache.toString()));
    }

    @DisplayName("상품 개수를 조회하면서 cache hit 이 발생하면, 쿼리는 실행되지 않는다.")
    @Test
    void countItemsBySearchCondWithCacheHit() {
        // given
        for (int i = 0; i < 11; i++) {
            createItem("Test Item" + i, 1000 * (i + 1));
        }

        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(1, 10, "id", "desc", null);

        // when
        itemService.countItemsBySearchCond(searchCond, 10);
        itemService.countItemsBySearchCond(searchCond, 10);

        // then
        verify(itemRepository, times(1)).countAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
    }

    @DisplayName("검색 조건에 검색어가 포함되면 캐시를 생성하지 않는다.")
    @Test
    void countItemsBySearchCondWithKeyword() {
        // given
        String cacheKey = "%s::count".formatted(CacheName.ITEMS_PAGE);
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(1, 10, "id", "desc", "Test");

        // when
        itemService.countItemsBySearchCond(searchCond, 11);

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
        assertThat(dataAfterCache).isNull();
        verify(itemRepository, times(1)).countAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
    }

    private Item createItem(String name, int price) {
        return itemJpaCommandRepository.save(Item.builder()
                .name(name)
                .price(price)
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

    private void createOrderItem(Item item, Order order, int quantity) {
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item)
                .name(item.getName())
                .price(item.getPrice())
                .quantity(quantity)
                .build();
        orderItemJpaRepository.save(orderItem);
    }

    private List<Item> convertItems(Object obj) {
        return om.convertValue(obj, new TypeReference<>() {});
    }
}
