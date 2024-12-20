package hhplus.ecommerce.server.integration.application;

import hhplus.ecommerce.server.application.OrderFacade;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.OutOfItemStockException;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.domain.order.service.OrderCommand;
import hhplus.ecommerce.server.domain.order.service.OrderInfo;
import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.exception.OutOfPointException;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.domain.order.message.OrderCreatedEvent;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemJpaCommandRepository;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemStockJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.point.PointJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.user.UserJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class OrderFacadeTest extends TestContainerEnvironment {

    @Autowired
    OrderFacade orderFacade;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    PointJpaRepository pointJpaRepository;

    @Autowired
    ItemJpaCommandRepository itemJpaCommandRepository;

    @Autowired
    ItemStockJpaRepository itemStockJpaRepository;

    @Autowired
    OrderJpaRepository orderJpaRepository;

    @Autowired
    OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    ApplicationEvents applicationEvents;

    @DisplayName("주문을 생성하고 이벤트를 발행할 수 있다.")
    @Test
    void createOrder() {
        // given
        User user = createUser("testUser");
        createPoint(50000, user);
        Item item1 = createItem("item1", 1000);
        createItemStock(10, item1);
        Item item2 = createItem("item2", 2000);
        createItemStock(20, item2);

        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder(
                user.getId(),
                List.of(
                        new OrderCommand.CreateOrderItem(item1.getId(), 10),
                        new OrderCommand.CreateOrderItem(item2.getId(), 20)
                ));

        // when
        Long orderId = orderFacade.createOrder(command);

        // then
        assertThat(orderId).isNotNull();
        Order order = orderJpaRepository.findById(orderId).orElseThrow();
        assertThat(order.getUser().getId()).isEqualTo(user.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(orderItemJpaRepository.findAllByOrderId(orderId)).hasSize(2)
                .extracting(oi -> tuple(oi.getItem().getId(), oi.getName(), oi.getPrice(), oi.getQuantity()))
                .containsExactlyInAnyOrder(
                        tuple(item1.getId(), item1.getName(), item1.getPrice(), 10),
                        tuple(item2.getId(), item2.getName(), item2.getPrice(), 20)
                );
        List<OrderCreatedEvent> events = applicationEvents.stream(OrderCreatedEvent.class).toList();
        assertThat(events).isNotEmpty();
    }

    @DisplayName("사용자의 주문 목록을 조회할 수 있다.")
    @Test
    void findOrders() {
        // given
        User user = createUser("testUser");
        Item item1 = createItem("item", 1000);
        Item item2 = createItem("item2", 2000);

        Order order = orderJpaRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.ORDERED)
                .build());
        orderItemJpaRepository.save(OrderItem.builder()
                .order(order)
                .item(item1)
                .name(item1.getName())
                .price(item1.getPrice())
                .quantity(10)
                .build());
        orderItemJpaRepository.save(OrderItem.builder()
                .order(order)
                .item(item2)
                .name(item2.getName())
                .price(item2.getPrice())
                .quantity(20)
                .build());

        // when
        List<OrderInfo.OrderDetail> result = orderFacade.findOrders(user.getId());

        // then
        assertThat(result).hasSize(1)
                .extracting(o -> tuple(o.id(), o.amount(), o.status()))
                .containsExactly(
                        tuple(order.getId(), 50000, OrderStatus.ORDERED)
                );
    }

    @DisplayName("주문 상세 정보를 조회할 수 있다.")
    @Test
    void getOrder() {
        // given
        User user = createUser("testUser");
        Item item1 = createItem("item", 1000);
        Item item2 = createItem("item2", 2000);

        Order order = orderJpaRepository.save(Order.builder()
                .user(user)
                .build());
        OrderItem orderItem1 = orderItemJpaRepository.save(OrderItem.builder()
                .order(order)
                .item(item1)
                .name(item1.getName())
                .price(item1.getPrice())
                .quantity(10)
                .build());
        OrderItem orderItem2 = orderItemJpaRepository.save(OrderItem.builder()
                .order(order)
                .item(item2)
                .name(item2.getName())
                .price(item2.getPrice())
                .quantity(20)
                .build());

        // when
        OrderInfo.OrderAndItemDetails result = orderFacade.getOrder(user.getId(), order.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderDetail().id()).isEqualTo(order.getId());
        assertThat(result.orderItemDetails()).hasSize(2)
                .extracting(oi -> tuple(oi.id(), oi.name(), oi.price(), oi.quantity()))
                .containsExactly(
                        tuple(orderItem1.getId(), item1.getName(), item1.getPrice(), orderItem1.getQuantity()),
                        tuple(orderItem2.getId(), item2.getName(), item2.getPrice(), orderItem2.getQuantity())
                );
    }

    @DisplayName("주문 시 재고수량이 부족하면 트랜잭션 보상 로직으로 차감했던 재고를 복원할 수 있다.")
    @Test
    void createOrder_withNotEnoughStock() {
        // given
        User user = createUser("testUser");
        createPoint(12000, user);
        Item item1 = createItem("item1", 1000);
        ItemStock itemStock1 = createItemStock(10, item1);
        Item item2 = createItem("item2", 2000);
        ItemStock itemStock2 = createItemStock(0, item2);

        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder(
                user.getId(),
                List.of(
                        new OrderCommand.CreateOrderItem(item1.getId(), 10),
                        new OrderCommand.CreateOrderItem(item2.getId(), 1)
                ));

        // when
        // then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(OutOfItemStockException.class)
                .hasMessage(new OutOfItemStockException(0).getMessage());
        List<ItemStock> itemStocks = itemStockJpaRepository.findAll();
        assertThat(itemStocks).hasSize(2)
                .extracting(is -> tuple(is.getId(), is.getAmount()))
                .containsExactlyInAnyOrder(
                        tuple(itemStock1.getId(), 10),
                        tuple(itemStock2.getId(), 0)
                );
    }

    @DisplayName("포인트 잔액이 부족하면 트랜잭션 보상 로직으로 차감했던 재고를 복원할 수 있다.")
    @Test
    void createOrder_withNotEnoughPoint() {
        // given
        int leftPoint = 9999;
        User user = createUser("testUser");
        createPoint(leftPoint, user);
        Item item = createItem("item", 1000);
        createItemStock(10, item);

        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder(
                user.getId(),
                List.of(
                        new OrderCommand.CreateOrderItem(item.getId(), 10)
                ));

        // when
        // then
        assertThatThrownBy(() -> orderFacade.createOrder(command))
                .isInstanceOf(OutOfPointException.class)
                .hasMessage(new OutOfPointException(leftPoint).getMessage());
        ItemStock itemStock = itemStockJpaRepository.findByItemId(item.getId()).orElseThrow();
        assertThat(itemStock.getAmount()).isEqualTo(10);
    }

    private User createUser(String username) {
        return userJpaRepository.save(User.builder()
                .username(username)
                .build());
    }

    private Point createPoint(int amount, User user) {
        return pointJpaRepository.save(Point.builder()
                .amount(amount)
                .user(user)
                .build());
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
}
