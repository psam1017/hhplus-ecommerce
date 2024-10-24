package hhplus.ecommerce.server.domain.order.service;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemStockException;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderCommand {

    public record CreateOrderByItem(
            Long userId,
            List<CreateOrderItem> items
    ) {
        public Set<Long> toItemIds() {
            return items.stream()
                    .map(CreateOrderItem::itemId)
                    .collect(Collectors.toSet());
        }

        public Map<Long, Integer> toItemMap() {
            return items.stream()
                    .collect(Collectors.toMap(
                            CreateOrderItem::itemId,
                            CreateOrderItem::amount
                    ));
        }

        public Order toOrder(User user) {
            return Order.builder()
                    .user(user)
                    .status(OrderStatus.ORDERED)
                    .orderDateTime(LocalDateTime.now())
                    .build();
        }

        public List<OrderItem> toOrderItems(List<Item> items, Order order) {
            return items.stream()
                    .map(item -> OrderItem.builder()
                            .order(order)
                            .item(item)
                            .name(item.getName())
                            .price(item.getPrice())
                            .quantity(getQuantity(item))
                            .build())
                    .collect(Collectors.toList());
        }

        private Integer getQuantity(Item item) {
            return this.items.stream()
                    .filter(i -> Objects.equals(i.itemId(), item.getId()))
                    .map(CreateOrderItem::amount)
                    .findAny()
                    .orElseThrow(NoSuchItemStockException::new);
        }
    }

    public record CreateOrderByCart(
            Long userId,
            Set<Long> cartIds
    ) {

        public Order toOrder(User user) {
            return Order.builder()
                    .user(user)
                    .status(OrderStatus.ORDERED)
                    .orderDateTime(LocalDateTime.now())
                    .build();
        }

        public List<OrderItem> toOrderItems(List<Item> items, Map<Long, Integer> itemIdStockAmountMap, Order order) {
            return items.stream()
                    .map(item -> OrderItem.builder()
                            .order(order)
                            .item(item)
                            .name(item.getName())
                            .price(item.getPrice())
                            .quantity(itemIdStockAmountMap.get(item.getId()))
                            .build())
                    .collect(Collectors.toList());
        }
    }

    public record CreateOrderItem(
            Long itemId,
            Integer amount
    ) {
    }
}
