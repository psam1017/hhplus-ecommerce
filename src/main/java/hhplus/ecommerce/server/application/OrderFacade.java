package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.cart.service.CartService;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.order.OrderItem;
import hhplus.ecommerce.server.domain.order.service.OrderCommand;
import hhplus.ecommerce.server.domain.order.service.OrderInfo;
import hhplus.ecommerce.server.domain.order.service.OrderService;
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

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private final UserService userService;
    private final PointService pointService;
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;
    private final OrderDataPlatform orderDataPlatform;

    /**
     * 주문 + 결제 VS 주문 -> 결제
     * 1. 주문과 결제를 하나로 하는 경우
     *  - 주문과 결제를 동시에 하는 정책인 경우에 적합하다.
     *  - 포인트로 결제하는 게 확정된 현재 상황에서 주문과 결제를 분리할 필요성이 낮다.
     *  - 주문과 동시에 재고관리를 수행하여 주문과 결제 사이에서 발생할 수 있는, 예상하지 못한 예외상황을 제거할 수 있다.
     * 2. 주문과 결제를 분리
     *  - 주문과 결제를 분리해야 할 정책인 경우에 적합하다.
     *  - 포인트 결제 이외에 다른 결제수단이 추가되는 경우 등의 확장에 대흥하기 좋다.
     *  - 현실에서도 주문과 결제가 별개의 행위이듯 주문과 결제라는 각각의 행위 자체에 대해 집중하여 고찰할 수 있다.
     * 3. 결론
     *  - 재고관리에 대한 동시성 제어를 더 중요시하는 서비스를 만들고자 예외상황을 줄일 수 있는 '주문 + 결제' API 를 제공한다.
     * @param command 사용자 주문 생성에 필요한 정보
     * @return 주문 ID
     */
    @Transactional
    public Long createOrder(OrderCommand.CreateOrder command) {
        Set<Long> itemIds = command.toItemIds();
        Map<Long, Integer> itemIdStockAmountMap = command.toItemMap();

        User user = userService.getUser(command.userId());
        List<Item> items = itemService.findItems(itemIds);
        itemService.deductStocks(itemIdStockAmountMap);
        pointService.usePoint(command.userId(), items, itemIdStockAmountMap);
        Order order = orderService.createOrderAndItems(command, user, items);
        cartService.deleteCartItems(command.userId(), itemIds);
        orderDataPlatform.saveOrderData(itemIdStockAmountMap);

        return order.getId();
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
