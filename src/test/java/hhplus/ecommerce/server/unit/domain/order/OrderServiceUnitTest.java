package hhplus.ecommerce.server.unit.domain.order;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.domain.order.exception.NoSuchOrderException;
import hhplus.ecommerce.server.domain.order.repository.OrderItemRepository;
import hhplus.ecommerce.server.domain.order.repository.OrderRepository;
import hhplus.ecommerce.server.domain.order.service.OrderCommand;
import hhplus.ecommerce.server.domain.order.service.OrderService;
import hhplus.ecommerce.server.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {

    @InjectMocks
    OrderService sut;

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderItemRepository orderItemRepository;

    @DisplayName("주문을 생성할 수 있다.")
    @Test
    void createOrderAndItems() {
        // given
        OrderCommand.CreateOrder command = new OrderCommand.CreateOrder(
                1L,
                List.of(
                        new OrderCommand.CreateOrderItem(1L, 1),
                        new OrderCommand.CreateOrderItem(2L, 2)
                )
        );
        User user = buildUser(1L);
        List<Item> items = List.of(
                buildItem(1L),
                buildItem(2L)
        );

        Order order = buildOrder(1L, user);
        List<OrderItem> orderItems = List.of(
                buildOrderItem(1L, "Item1", 1000, 1, order, buildItem(1L)),
                buildOrderItem(2L, "Item2", 2000, 2, order, buildItem(2L))
        );

        when(orderRepository.save(any()))
                .thenReturn(order);
        when(orderItemRepository.saveAll(anyList()))
                .thenReturn(orderItems);

        // when
        Order result = sut.createOrderAndItems(command, user, items);

        // then
        assertThat(result).isEqualTo(order);
        verify(orderRepository, times(1)).save(any());
        verify(orderItemRepository, times(1)).saveAll(anyList());
    }

    @DisplayName("사용자의 주문 목록을 조회할 수 있다.")
    @Test
    void findAllByUserId() {
        // given
        Long userId = 1L;
        User user = buildUser(userId);

        List<Order> orders = List.of(
                buildOrder(1L, user),
                buildOrder(2L, user)
        );

        when(orderRepository.findAllByUserId(userId)).thenReturn(orders);

        // when
        List<Order> result = sut.findAllByUserId(userId);

        // then
        assertThat(result).hasSize(2)
                .extracting(o -> tuple(o.getId(), o.getUser().getId()))
                .containsExactly(
                        tuple(1L, userId),
                        tuple(2L, userId)
                );
        verify(orderRepository, times(1)).findAllByUserId(userId);
    }

    @DisplayName("주문을 조회할 수 있다.")
    @Test
    void getOrder() {
        // given
        Long orderId = 1L;
        Long userId = 1L;
        Order order = buildOrder(orderId, buildUser(userId));

        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        // when
        Order result = sut.getOrder(orderId, userId);

        // then
        assertThat(result).isEqualTo(order);
        verify(orderRepository, times(1)).findByIdAndUserId(orderId, userId);
    }

    @DisplayName("존재하지 않는 주문을 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchOrderExceptionWhenGetOrder() {
        // given
        Long orderId = 1L;
        Long userId = 1L;

        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> sut.getOrder(orderId, userId))
                .isInstanceOf(NoSuchOrderException.class)
                .hasMessage(new NoSuchOrderException().getMessage());
        verify(orderRepository, times(1)).findByIdAndUserId(orderId, userId);
    }

    @DisplayName("주문 상품 목록을 조회할 수 있다.")
    @Test
    void findOrderItems() {
        // given
        Long orderId = 1L;
        Order order = buildOrder(orderId, buildUser(1L));

        List<OrderItem> orderItems = List.of(
                buildOrderItem(1L, "Item1", 1000, 1, order, buildItem(1L)),
                buildOrderItem(2L, "Item2", 2000, 2, order, buildItem(2L))
        );

        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(orderItems);

        // when
        List<OrderItem> result = sut.findOrderItems(orderId);

        // then
        assertThat(result).hasSize(2)
                .extracting(oi -> tuple(oi.getId(), oi.getName(), oi.getPrice(), oi.getQuantity()))
                .containsExactly(
                        tuple(1L, "Item1", 1000, 1),
                        tuple(2L, "Item2", 2000, 2)
                );
        verify(orderItemRepository, times(1)).findAllByOrderId(orderId);
    }

    @DisplayName("주문 목록별 총 상품 금액을 조회할 수 있다.")
    @Test
    void findOrderAmounts() {
        // given
        List<Long> orderIds = List.of(1L, 2L);
        Map<Long, Integer> orderAmounts = Map.of(
                1L, 1000,
                2L, 2000
        );

        when(orderItemRepository.findOrderAmounts(orderIds)).thenReturn(orderAmounts);

        // when
        Map<Long, Integer> result = sut.findOrderAmounts(orderIds);

        // then
        assertThat(result).hasSize(2)
                .containsEntry(1L, 1000)
                .containsEntry(2L, 2000);
        verify(orderItemRepository, times(1)).findOrderAmounts(orderIds);
    }

    @DisplayName("주문을 취소할 수 있다.")
    @Test
    void cancelOrder() {
        // given
        Long orderId = 1L;
        Order order = buildOrder(orderId, buildUser(1L));

        List<OrderItem> orderItems = List.of(
                buildOrderItem(1L, "Item1", 1000, 1, order, buildItem(1L)),
                buildOrderItem(2L, "Item2", 2000, 2, order, buildItem(2L))
        );

        when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(orderItems);

        // when
        sut.cancelOrder(orderId);

        // then
        verify(orderItemRepository, times(1)).findAllByOrderId(orderId);
        verify(orderItemRepository, times(1)).deleteAll(orderItems);
        verify(orderRepository, times(1)).deleteById(orderId);
    }

    @DisplayName("매출 상위 상품 아이디 목록을 조회할 수 있다.")
    @Test
    void findTopItemIds() {
        // given
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(3);
        LocalDateTime endDateTime = LocalDateTime.now();

        List<Long> topItemIds = List.of(1L, 2L, 3L);

        when(orderItemRepository.findTopItemIds(startDateTime, endDateTime)).thenReturn(topItemIds);

        // when
        List<Long> result = sut.findTopItemIds(startDateTime, endDateTime);

        // then
        assertThat(result).hasSize(3).containsExactly(1L, 2L, 3L);
        verify(orderItemRepository, times(1)).findTopItemIds(startDateTime, endDateTime);
    }

    private User buildUser(Long userId) {
        return User.builder().id(userId).build();
    }

    private Item buildItem(Long itemId) {
        return Item.builder().id(itemId).build();
    }

    private Order buildOrder(Long orderId, User user) {
        return Order.builder().id(orderId).status(OrderStatus.ORDERED).orderDateTime(LocalDateTime.now()).user(user).build();
    }

    private OrderItem buildOrderItem(Long id, String name, int price, int quantity, Order order, Item item) {
        return OrderItem.builder().id(id).name(name).price(price).quantity(quantity).order(order).item(item).build();
    }
}
