package hhplus.ecommerce.server.integration.domain.item;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemException;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemStockException;
import hhplus.ecommerce.server.domain.item.exception.OutOfItemStockException;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.infrastructure.item.ItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.item.ItemStockJpaRepository;
import hhplus.ecommerce.server.infrastructure.order.OrderItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.order.OrderJpaRepository;
import hhplus.ecommerce.server.integration.domain.ServiceTestEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class ItemServiceTest extends ServiceTestEnvironment {

    @Autowired
    ItemService sut;

    @Autowired
    ItemJpaRepository itemJpaRepository;

    @Autowired
    ItemStockJpaRepository itemStockJpaRepository;

    @Autowired
    OrderJpaRepository orderJpaRepository;

    @Autowired
    OrderItemJpaRepository orderItemJpaRepository;

    @DisplayName("가장 인기 있는 상품들을 조회할 수 있다.")
    @Test
    void findTopItems() {
        // given
        Item item1 = createItem("Test Item1", 1000);
        Item item2 = createItem("Test Item2", 2000);
        Item item3 = createItem("Test Item3", 3000);
        createItemStock(10, item1);
        createItemStock(4, item2);
        createItemStock(2, item3);

        Order order = createOrder();
        createOrderItem(item1, order, 10);
        createOrderItem(item2, order, 4);
        createOrderItem(item3, order, 2);

        // when
        List<Item> result = sut.findTopItems();

        // then
        assertThat(result).hasSize(3)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(
                        tuple(item1.getId(), item1.getName(), item1.getPrice()),
                        tuple(item2.getId(), item2.getName(), item2.getPrice()),
                        tuple(item3.getId(), item3.getName(), item3.getPrice())
                );
    }

    @DisplayName("모든 상품을 조회할 수 있다.")
    @Test
    void findItems() {
        // given
        Item item1 = createItem("Test Item1", 1000);
        Item item2 = createItem("Test Item2", 2000);

        // when
        List<Item> result = sut.findItems();

        // then
        assertThat(result).hasSize(2)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactlyInAnyOrder(
                        tuple(item1.getId(), item1.getName(), item1.getPrice()),
                        tuple(item2.getId(), item2.getName(), item2.getPrice())
                );
    }

    @DisplayName("특정 아이디들의 상품을 조회할 수 있다.")
    @Test
    void findItemsByIds() {
        // given
        Item item1 = createItem("Test Item1", 1000);
        Item item2 = createItem("Test Item2", 2000);
        Set<Long> itemIds = Set.of(item1.getId(), item2.getId());

        // when
        List<Item> result = sut.findItems(itemIds);

        // then
        assertThat(result).hasSize(2)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactlyInAnyOrder(
                        tuple(item1.getId(), item1.getName(), item1.getPrice()),
                        tuple(item2.getId(), item2.getName(), item2.getPrice())
                );
    }

    @DisplayName("존재하지 않는 아이디로 상품을 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemExceptionWhenFindItemsByIds() {
        // given
        Set<Long> itemIds = Set.of(1L, 2L);

        // when
        // then
        assertThatThrownBy(() -> sut.findItems(itemIds))
                .isInstanceOf(NoSuchItemException.class)
                .hasMessage(new NoSuchItemException().getMessage());
    }

    @DisplayName("상품을 원하는 수량만큼 차감할 수 있다.")
    @Test
    void deductStocks() {
        // given
        int stockAmount1 = 10;
        Item item1 = createItem("Test Item1", 1000);
        ItemStock itemStock1 = createItemStock(stockAmount1, item1);

        int stockAmount2 = 20;
        Item item2 = createItem("Test Item2", 2000);
        ItemStock itemStock2 = createItemStock(stockAmount2, item2);

        Map<Long, Integer> itemIdStockAmountMap = Map.of(
                item1.getId(), stockAmount1,
                item2.getId(), stockAmount2
        );

        // when
        sut.deductStocks(itemIdStockAmountMap);

        // then
        List<ItemStock> itemStocks = itemStockJpaRepository.findAllByItemIdIn(itemIdStockAmountMap.keySet());
        assertThat(itemStocks).hasSize(2)
                .extracting(is -> tuple(is.getId(), is.getItem().getId(), is.getAmount()))
                .containsExactlyInAnyOrder(
                        tuple(itemStock1.getId(), item1.getId(), 0),
                        tuple(itemStock2.getId(), item2.getId(), 0)
                );
    }

    @DisplayName("존재하지 않는 아이디로 상품 재고를 차감할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemStockExceptionWhenDeductStocks() {
        // given
        Map<Long, Integer> itemIdStockAmountMap = Map.of(1L, 10, 2L, 20);

        // when
        // then
        assertThatThrownBy(() -> sut.deductStocks(itemIdStockAmountMap))
                .isInstanceOf(NoSuchItemStockException.class)
                .hasMessage(new NoSuchItemStockException().getMessage());
    }

    @DisplayName("재고가 부족하면 상품 재고를 차감할 수 없다.")
    @Test
    void throwNoSuchItemStockExceptionWhenDeductStocksNotEnoughStock() {
        // given
        int stockAmount = 10;
        Item item = createItem("Test Item", 1000);
        createItemStock(stockAmount, item);

        Map<Long, Integer> itemIdStockAmountMap = Map.of(item.getId(), stockAmount + 1);

        // when
        // then
        assertThatThrownBy(() -> sut.deductStocks(itemIdStockAmountMap))
                .isInstanceOf(OutOfItemStockException.class)
                .hasMessage(new OutOfItemStockException(stockAmount).getMessage());
    }

    @DisplayName("상품을 조회할 수 있다.")
    @Test
    void getItem() {
        // given
        Item item = createItem("Test Item", 1000);

        // when
        Item result = sut.getItem(item.getId());

        // then
        assertThat(result).extracting(Item::getId, Item::getName, Item::getPrice)
                .containsExactly(item.getId(), item.getName(), item.getPrice());
    }

    @DisplayName("존재하지 않는 아이디로 상품을 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemExceptionWhenGetItem() {
        // given
        Long itemId = 1L;

        // when
        // then
        assertThatThrownBy(() -> sut.getItem(itemId))
                .isInstanceOf(NoSuchItemException.class)
                .hasMessage(new NoSuchItemException().getMessage());
    }

    @DisplayName("여러 상품의 재고를 조회할 수 있다.")
    @Test
    void getStocks() {
        // given
        Item item1 = createItem("Test Item1", 1000);
        ItemStock itemStock1 = createItemStock(10, item1);

        Item item2 = createItem("Test Item2", 2000);
        ItemStock itemStock2 = createItemStock(20, item2);

        Set<Long> itemIds = Set.of(item1.getId(), item2.getId());

        // when
        Map<Long, Integer> result = sut.getStocks(itemIds);

        // then
        assertThat(result).hasSize(2)
                .containsEntry(item1.getId(), itemStock1.getAmount())
                .containsEntry(item2.getId(), itemStock2.getAmount());
    }

    @DisplayName("존재하지 않는 아이디로 상품 재고를 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemStockExceptionWhenGetStocks() {
        // given
        Set<Long> itemIds = Set.of(1L, 2L);

        // when
        // then
        assertThatThrownBy(() -> sut.getStocks(itemIds))
                .isInstanceOf(NoSuchItemStockException.class)
                .hasMessage(new NoSuchItemStockException().getMessage());
    }

    @DisplayName("상품 재고를 조회할 수 있다.")
    @Test
    void getItemStockByItemId() {
        // given
        Item item = createItem("Test Item", 1000);
        ItemStock itemStock = createItemStock(10, item);

        // when
        ItemStock result = sut.getItemStockByItemId(item.getId());

        // then
        assertThat(result)
                .extracting(is -> tuple(is.getId(), is.getItem().getId(), is.getItem().getName(), is.getItem().getPrice(), is.getAmount()))
                .isEqualTo(tuple(itemStock.getId(), item.getId(), item.getName(), item.getPrice(), itemStock.getAmount()));
    }

    @DisplayName("존재하지 않는 아이디로 상품 재고를 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemStockExceptionWhenGetItemStockByItemId() {
        // given
        Long itemId = 1L;

        // when
        // then
        assertThatThrownBy(() -> sut.getItemStockByItemId(itemId))
                .isInstanceOf(NoSuchItemStockException.class)
                .hasMessage(new NoSuchItemStockException().getMessage());
    }

    private Item createItem(String name, int price) {
        return itemJpaRepository.save(Item.builder()
                .name(name)
                .price(price)
                .build());
    }

    private ItemStock createItemStock(int amount, Item item) {
        return itemStockJpaRepository.save(ItemStock.builder()
                .amount(amount)
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

    private void createOrderItem(Item item1, Order order, int quantity) {
        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .item(item1)
                .name(item1.getName())
                .price(item1.getPrice())
                .quantity(quantity)
                .build();
        orderItemJpaRepository.save(orderItem);
    }
}
