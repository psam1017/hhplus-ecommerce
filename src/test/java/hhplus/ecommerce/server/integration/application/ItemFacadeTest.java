package hhplus.ecommerce.server.integration.application;

import hhplus.ecommerce.server.application.ItemFacade;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.service.ItemInfo;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.infrastructure.item.ItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.item.ItemStockJpaRepository;
import hhplus.ecommerce.server.infrastructure.order.OrderItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.order.OrderJpaRepository;
import hhplus.ecommerce.server.integration.TransactionalTestEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class ItemFacadeTest extends TransactionalTestEnvironment {

    @Autowired
    ItemFacade itemFacade;

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
        List<ItemInfo.ItemDetail> result = itemFacade.findTopItems();

        // then
        assertThat(result).hasSize(3)
                .extracting(i -> tuple(i.id(), i.name(), i.price()))
                .containsExactlyInAnyOrder(
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
        ItemStock itemStock1 = createItemStock(10, item1);
        ItemStock itemStock2 = createItemStock(20, item2);

        // when
        List<ItemInfo.ItemDetail> result = itemFacade.findItems();

        // then
        assertThat(result).hasSize(2)
                .extracting(i -> tuple(i.id(), i.name(), i.price(), i.amount()))
                .containsExactlyInAnyOrder(
                        tuple(item1.getId(), item1.getName(), item1.getPrice(), itemStock1.getAmount()),
                        tuple(item2.getId(), item2.getName(), item2.getPrice(), itemStock2.getAmount())
                );
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
