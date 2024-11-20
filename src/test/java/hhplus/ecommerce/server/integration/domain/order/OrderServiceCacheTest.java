package hhplus.ecommerce.server.integration.domain.order;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemRepository;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.domain.order.repository.OrderItemRepository;
import hhplus.ecommerce.server.domain.order.service.OrderService;
import hhplus.ecommerce.server.infrastructure.cache.CacheName;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemJpaCommandRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class OrderServiceCacheTest extends TestContainerEnvironment {

    @Autowired
    OrderService orderService;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @SpyBean
    ItemRepository itemRepository;

    @SpyBean
    OrderItemRepository orderItemRepository;

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

        LocalDateTime endDateTime = LocalDate.now().atStartOfDay();
        LocalDateTime startDateTime = endDateTime.minusDays(3);

        // when
        List<Long> result = orderService.findTopItemIds(startDateTime, endDateTime);

        // then
        Object dataAfterCache = redisTemplate.opsForValue().get(cacheKey);
        List<Long> cachedItems = convertItems(dataAfterCache);
        Long topItemId = cachedItems.get(0);

        assertThat(dataBeforeCache).isNull();
        assertThat(dataAfterCache).isNotNull();
        assertThat(result).hasSize(1)
                .containsExactly(item1.getId())
                .containsExactly(topItemId)
                .doesNotContain(item2.getId());
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

        LocalDateTime endDateTime = LocalDate.now().atStartOfDay();
        LocalDateTime startDateTime = endDateTime.minusDays(3);

        // when
        orderService.findTopItemIds(startDateTime, endDateTime);
        orderService.findTopItemIds(startDateTime, endDateTime);

        // then
        verify(orderItemRepository, times(1)).findTopItemIds(any(LocalDateTime.class), any(LocalDateTime.class));
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

    private List<Long> convertItems(Object obj) {
        return om.convertValue(obj, new TypeReference<>() {});
    }
}
