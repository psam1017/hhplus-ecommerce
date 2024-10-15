package hhplus.ecommerce.server.integration.domain.order;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.domain.order.exception.NoSuchOrderException;
import hhplus.ecommerce.server.domain.order.service.OrderService;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.item.ItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.item.ItemStockJpaRepository;
import hhplus.ecommerce.server.infrastructure.order.OrderItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.order.OrderJpaRepository;
import hhplus.ecommerce.server.infrastructure.user.UserJpaRepository;
import hhplus.ecommerce.server.integration.domain.ServiceTestEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class OrderServiceTest extends ServiceTestEnvironment {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderJpaRepository orderJpaRepository;

    @Autowired
    OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    ItemJpaRepository itemJpaRepository;

    @Autowired
    ItemStockJpaRepository itemStockJpaRepository;

    @DisplayName("주문을 생성할 수 있다.")
    @Test
    void createOrder() {
        // given
        User user = createUser("TestUser");
        Order order = createOrder(user);

        // when
        Order result = orderService.createOrder(order);

        // then
        assertThat(result).isEqualTo(order);
        assertThat(result.getUser().getId()).isEqualTo(user.getId());
    }

    @DisplayName("여러 주문 상품을 생성할 수 있다.")
    @Test
    void createOrderItems() {
        // given
        User user = createUser("TestUser");
        Order order = createOrder(user);
        Item item1 = createItem("Item1", 1000);
        Item item2 = createItem("Item2", 2000);

        List<OrderItem> orderItems = List.of(
                OrderItem.builder().name("Item1").price(1000).quantity(1).order(order).item(item1).build(),
                OrderItem.builder().name("Item2").price(2000).quantity(2).order(order).item(item2).build()
        );

        // when
        List<OrderItem> result = orderService.createOrderItems(orderItems);

        // then
        assertThat(result).hasSize(2)
                .extracting("name", "price", "quantity")
                .containsExactly(
                        tuple("Item1", 1000, 1),
                        tuple("Item2", 2000, 2)
                );
    }

    @DisplayName("사용자의 주문 목록을 조회할 수 있다.")
    @Test
    void findAllByUserId() {
        // given
        User user = createUser("TestUser");
        Order order1 = createOrder(user);
        Order order2 = createOrder(user);

        // when
        List<Order> result = orderService.findAllByUserId(user.getId());

        // then
        assertThat(result).hasSize(2)
                .extracting(o -> tuple(o.getId(), o.getUser().getId()))
                .containsExactly(
                        tuple(order1.getId(), user.getId()),
                        tuple(order2.getId(), user.getId())
                );
    }

    @DisplayName("주문을 조회할 수 있다.")
    @Test
    void getOrder() {
        // given
        User user = createUser("TestUser");
        Order order = createOrder(user);

        // when
        Order result = orderService.getOrder(order.getId(), user.getId());

        // then
        assertThat(result).isEqualTo(order);
    }

    @DisplayName("존재하지 않는 주문을 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchOrderExceptionWhenGetOrder() {
        // given
        Long orderId = 1L;
        Long userId = 1L;

        // when
        // then
        assertThatThrownBy(() -> orderService.getOrder(orderId, userId))
                .isInstanceOf(NoSuchOrderException.class)
                .hasMessage(new NoSuchOrderException().getMessage());
    }

    @DisplayName("주문 상품 목록을 조회할 수 있다.")
    @Test
    void findOrderItems() {
        // given
        User user = createUser("TestUser");
        Order order = createOrder(user);
        Item item1 = createItem("Item1", 1000);
        Item item2 = createItem("Item2", 2000);
        OrderItem orderItem1 = createOrderItem("Item1", 1000, 1, order, item1);
        OrderItem orderItem2 = createOrderItem("Item2", 2000, 2, order, item2);

        // when
        List<OrderItem> result = orderService.findOrderItems(order.getId());

        // then
        assertThat(result).hasSize(2)
                .extracting(OrderItem::getId, OrderItem::getName, OrderItem::getPrice, OrderItem::getQuantity)
                .containsExactly(
                        tuple(orderItem1.getId(), "Item1", 1000, 1),
                        tuple(orderItem2.getId(), "Item2", 2000, 2)
                );
    }

    @DisplayName("주문 목록별 총 상품 금액을 조회할 수 있다.")
    @Test
    void findOrderAmounts() {
        // given
        User user = createUser("TestUser");
        Order order1 = createOrder(user);
        Order order2 = createOrder(user);
        Item item1 = createItem("Item1", 1000);
        Item item2 = createItem("Item2", 2000);
        createOrderItem("Item1", 1000, 1, order1, item1);
        createOrderItem("Item2", 2000, 2, order2, item2);

        List<Long> orderIds = List.of(order1.getId(), order2.getId());

        // when
        Map<Long, Integer> result = orderService.findOrderAmounts(orderIds);

        // then
        assertThat(result).hasSize(2)
                .containsEntry(order1.getId(), 1000)
                .containsEntry(order2.getId(), 4000);
    }

    private User createUser(String username) {
        return userJpaRepository.save(User.builder()
                .username(username)
                .build());
    }

    private Item createItem(String name, int price) {
        Item item = itemJpaRepository.save(Item.builder()
                .name(name)
                .price(price)
                .build());
        itemStockJpaRepository.save(ItemStock.builder()
                .amount(10)
                .item(item)
                .build());
        return item;
    }

    private Order createOrder(User user) {
        return orderJpaRepository.save(Order.builder()
                .status(OrderStatus.ORDERED)
                .orderDateTime(LocalDateTime.now())
                .user(user)
                .build());
    }

    private OrderItem createOrderItem(String name, int price, int quantity, Order order, Item item) {
        return orderItemJpaRepository.save(OrderItem.builder()
                .name(name)
                .price(price)
                .quantity(quantity)
                .order(order)
                .item(item)
                .build());
    }
}