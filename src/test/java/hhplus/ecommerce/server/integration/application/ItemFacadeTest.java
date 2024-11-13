package hhplus.ecommerce.server.integration.application;

import hhplus.ecommerce.server.application.ItemFacade;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.item.service.ItemInfo;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemJpaCommandRepository;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemStockJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ItemFacadeTest extends TestContainerEnvironment {

    @Autowired
    ItemFacade itemFacade;

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

        LocalDateTime endDateTime = LocalDate.now().atStartOfDay();
        LocalDateTime startDateTime = endDateTime.minusDays(3);

        // when
        List<ItemInfo.ItemDetail> result = itemFacade.findTopItems(startDateTime, endDateTime);

        // then
        assertThat(result).hasSize(3)
                .extracting(i -> tuple(i.id(), i.name(), i.price()))
                .containsExactly(
                        tuple(item1.getId(), item1.getName(), item1.getPrice()),
                        tuple(item2.getId(), item2.getName(), item2.getPrice()),
                        tuple(item3.getId(), item3.getName(), item3.getPrice())
                );
    }

    @DisplayName("상품을 페이징 조회할 수 있다.")
    @Test
    void findItems() {
        // given
        int totalItems = 11;
        int size = totalItems - 1;
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < totalItems; i++) {
            Item item = createItem("Test Item" + i, 1000 * (i + 1));
            createItemStock(size * (i + 1), item);
            items.add(item);
        }

        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(1, 2, "id", "desc", null);

        // when
        ItemInfo.ItemPageInfo pageInfo = itemFacade.pageItems(searchCond);

        // then
        List<ItemInfo.ItemDetail> details = pageInfo.itemDetails();
        Collections.reverse(items);
        List<Long> ids = items.subList(0, size).stream().map(Item::getId).toList();
        assertThat(details).hasSize(size)
                .extracting(ItemInfo.ItemDetail::id)
                .containsExactlyElementsOf(ids)
                .doesNotContain(items.get(items.size() - 1).getId());
        assertThat(pageInfo.totalCount()).isEqualTo(totalItems);
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
