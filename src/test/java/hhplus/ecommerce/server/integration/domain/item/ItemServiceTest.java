package hhplus.ecommerce.server.integration.domain.item;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemException;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemStockException;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemJpaCommandRepository;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemStockJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

public class ItemServiceTest extends TestContainerEnvironment {

    @Autowired
    ItemService sut;

    @Autowired
    ItemJpaCommandRepository itemJpaCommandRepository;

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

    @DisplayName("상품을 페이지 조회 개수 만큼만 조회할 수 있다. page=1")
    @Test
    void findItemsBySize() {
        // given
        int totalItems = 4;
        int size = totalItems - 1;
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < totalItems; i++) {
            items.add(createItem("Test Item" + i, 1000 * (i + 1)));
        }

        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(1, size, "id", "desc", null);

        // when
        List<Item> result = sut.findItemsBySearchCond(searchCond);

        // then
        Collections.reverse(items);
        List<Tuple> expected = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            expected.add(tuple(items.get(i).getId(), items.get(i).getName(), items.get(i).getPrice()));
        }
        assertThat(result).hasSize(size)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactlyElementsOf(expected);
    }

    @DisplayName("특정 페이지의 상품을 조회할 수 있다. page=2")
    @Test
    void findItemsByPage() {
        // given
        int totalItems = 9;
        int size = 5;
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < totalItems; i++) {
            items.add(createItem("Test Item" + i, 1000 * (i + 1)));
        }

        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(2, size, "id", "desc", null);

        // when
        List<Item> result = sut.findItemsBySearchCond(searchCond);

        // then
        Collections.reverse(items);
        List<Tuple> expected = new ArrayList<>();
        for (int i = size; i < totalItems; i++) {
            expected.add(tuple(items.get(i).getId(), items.get(i).getName(), items.get(i).getPrice()));
        }
        assertThat(result).hasSize(4)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactlyElementsOf(expected);
    }

    @DisplayName("상품을 이름 순서 오름차순으로 조회할 수 있다. prop=name, dir=asc")
    @Test
    void findItemsByNameAsc() {
        // given
        Item item1 = createItem("B", 1000);
        Item item2 = createItem("A", 2000);
        Item item3 = createItem("C", 3000);

        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(1, 3, "name", "asc", null);

        // when
        List<Item> result = sut.findItemsBySearchCond(searchCond);

        // then
        assertThat(result).hasSize(3)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(
                        tuple(item2.getId(), item2.getName(), item2.getPrice()),
                        tuple(item1.getId(), item1.getName(), item1.getPrice()),
                        tuple(item3.getId(), item3.getName(), item3.getPrice())
                );
    }

    @DisplayName("상품 이름을 검색하여 조회할 수 있다. keyword=A")
    @Test
    void findItemsByKeyword() {
        // given
        Item item1 = createItem("Apple", 1000);
        Item item2 = createItem("Banana", 2000);
        Item item3 = createItem("Cherry", 3000);

        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(1, 3, "id", "desc", "App");

        // when
        List<Item> result = sut.findItemsBySearchCond(searchCond);

        // then
        assertThat(result).hasSize(1)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(tuple(item1.getId(), item1.getName(), item1.getPrice()))
                .doesNotContain(
                        tuple(item2.getId(), item2.getName(), item2.getPrice()),
                        tuple(item3.getId(), item3.getName(), item3.getPrice())
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

    @DisplayName("상품의 재고수량을 차감할 수 있다.")
    @Test
    void deductStock() {
        // given
        int stockAmount = 10;
        int deductAmount = 5;
        Item item = createItem("Test Item", 1000);
        ItemStock itemStock = createItemStock(stockAmount, item);

        // when
        sut.deductStock(item.getId(), deductAmount);

        // then
        ItemStock result = itemStockJpaRepository.findById(itemStock.getId()).orElseThrow();
        assertThat(result.getAmount()).isEqualTo(stockAmount - deductAmount);
    }

    @DisplayName("존재하지 않는 아이디로 상품 재고수량을 차감할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemStockExceptionWhenDeductStock() {
        // given
        Long itemStockId = 1L;
        int deductAmount = 5;

        // when
        // then
        assertThatThrownBy(() -> sut.deductStock(itemStockId, deductAmount))
                .isInstanceOf(NoSuchItemStockException.class)
                .hasMessage(new NoSuchItemStockException().getMessage());
    }

    @DisplayName("상품의 재고수량을 복원할 수 있다.")
    @Test
    void restoreStock() {
        // given
        int stockAmount = 10;
        int restoreAmount = 5;
        Item item = createItem("Test Item", 1000);
        ItemStock itemStock = createItemStock(stockAmount, item);

        // when
        sut.restoreStock(item.getId(), restoreAmount);

        // then
        ItemStock result = itemStockJpaRepository.findById(itemStock.getId()).orElseThrow();
        assertThat(result.getAmount()).isEqualTo(stockAmount + restoreAmount);
    }

    @DisplayName("존재하지 않는 아이디로 상품 재고수량을 복원할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemStockExceptionWhenRestoreStock() {
        // given
        Long itemStockId = 1L;
        int restoreAmount = 5;

        // when
        // then
        assertThatThrownBy(() -> sut.restoreStock(itemStockId, restoreAmount))
                .isInstanceOf(NoSuchItemStockException.class)
                .hasMessage(new NoSuchItemStockException().getMessage());
    }

    private Item createItem(String name, int price) {
        return itemJpaCommandRepository.save(Item.builder()
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
