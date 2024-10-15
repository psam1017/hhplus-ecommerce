package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.cart.service.CartService;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.service.OrderCommand;
import hhplus.ecommerce.server.domain.order.service.OrderInfo;
import hhplus.ecommerce.server.domain.order.service.OrderService;
import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.service.PointService;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.domain.user.service.UserService;
import hhplus.ecommerce.server.infrastructure.data.OrderDataPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private final UserService userService;
    private final PointService pointService;
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;
    private final OrderDataPlatform orderDataPlatform;

    @Transactional
    public Long createOrder(OrderCommand.CreateOrder command) {
        Set<Long> itemIds = command.toItemIds();
        Map<Long, Integer> itemIdStockAmountMap = command.toItemMap();

        List<ItemStock> stocks = itemService.getItemStocksWithLock(itemIds);
        stocks.forEach(stock -> stock.deductStock(itemIdStockAmountMap.get(stock.getItem().getId())));

        User user = userService.getUser(command.userId());
        Point point = pointService.getPointByUserIdWithLock(user.getId());
        List<Item> items = itemService.findItems(itemIds);

        int totalPrice = calculateTotalPrice(itemIdStockAmountMap, items);
        point.usePoint(totalPrice);

        Order order = orderService.createOrder(command.toOrder(user));
        List<OrderItem> orderItems = orderService.createOrderItems(command.toOrderItems(items, order));

        cartService.deleteCartItems(command.userId(), itemIds);

        orderDataPlatform.saveOrderData(
                orderItems.stream()
                        .collect(Collectors.toMap(
                                orderItem -> orderItem.getItem().getId(),
                                OrderItem::getQuantity
                        ))
        );

        return order.getId();
    }

    private static int calculateTotalPrice(Map<Long, Integer> itemIdStockAmountMap, List<Item> items) {
        int totalPrice = 0;
        for (Item item : items) {
            totalPrice += item.getPrice() * itemIdStockAmountMap.get(item.getId());
        }
        return totalPrice;
    }

    public List<OrderInfo.OrderDetail> findOrders(Long userId) {
        List<Order> orders = orderService.findAllByUserId(userId);
        Map<Long, Integer> orderIdAmountMap = orderService.findOrderAmounts(orders.stream().map(Order::getId).toList());
        return orders.stream()
                .map(order -> OrderInfo.OrderDetail.from(order, orderIdAmountMap.get(order.getId())))
                .toList();
    }

    public OrderInfo.OrderAndItemDetails getOrder(Long userId, Long orderId) {
        Order order = orderService.getOrder(orderId, userId);
        List<OrderItem> orderItems = orderService.findOrderItems(orderId);
        return OrderInfo.OrderAndItemDetails.from(order, orderItems);
    }
}
