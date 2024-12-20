package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.cart.Cart;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderFacade {

    private final UserService userService;
    private final PointService pointService;
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;

    /**
     * 주문 + 결제 VS 주문 -> 결제
     * 1. 주문과 결제를 하나로 하는 경우
     *  - 주문과 결제를 동시에 하는 정책인 경우에 적합하다.
     *  - 포인트로 결제하는 게 확정된 현재 상황에서 주문과 결제를 분리할 필요성이 낮다.
     *  - 주문과 동시에 재고관리를 수행하여 주문과 결제 사이에서 발생할 수 있는, 예상하지 못한 예외상황을 제거할 수 있다.
     * 2. 주문과 결제를 분리
     *  - 주문과 결제를 분리해야 할 정책인 경우에 적합하다.
     *  - 포인트 결제 이외에 다른 결제수단이 추가되는 경우 등의 확장에 대응하기 좋다.
     *  - 현실에서도 주문과 결제가 별개의 행위이듯 주문과 결제라는 각각의 행위 자체에 대해 집중하여 고찰할 수 있다.
     * 3. 결론
     *  - 재고관리에 대한 동시성 제어를 더 중요시하는 서비스를 만들고자 예외상황을 줄일 수 있는 '주문 + 결제' API 를 제공한다.
     * @param command 사용자 주문 생성에 필요한 정보
     * @return 주문 ID
     */
    public Long createOrder(OrderCommand.CreateOrder command) {

        Deque<Runnable> compensationActions = new ArrayDeque<>();

        try {
            return processOrder(command, compensationActions);
        } catch (Exception e) {
            while (!compensationActions.isEmpty()) {
                try {
                    compensationActions.pop().run();
                } catch (Exception e2) {
                    // 로그 기록, 추가 보상 작업, 알림 전송, 모니터링 시스템 연동 등
                    log.error("compensation action failed", e2);
                }
            }
            throw e;
        }
    }

    private Long processOrder(OrderCommand.CreateOrder command, Deque<Runnable> compensationActions) {
        Set<Long> itemIds = command.toItemIds();
        Map<Long, Integer> itemIdStockAmountMap = command.toItemMap();
        User user = userService.getUser(command.userId());
        List<Item> items = itemService.findItems(itemIds);

        for (Long itemId : itemIdStockAmountMap.keySet()) {
            ItemStock itemStock = itemService.getItemStockByItemId(itemId);
            itemService.deductStock(itemStock.getId(), itemIdStockAmountMap.get(itemId));
            compensationActions.push(() -> itemService.restoreStock(itemStock.getId(), itemIdStockAmountMap.get(itemId)));
        }

        Point point = pointService.getPointByUserId(command.userId());
        int usedPoint = pointService.usePoint(point.getId(), items, itemIdStockAmountMap);
        compensationActions.push(() -> pointService.chargePoint(point.getId(), usedPoint));

        Order order = orderService.createOrderAndItems(command, user, items);
        compensationActions.push(() -> orderService.cancelOrder(order.getId()));

        cartService.deleteCartItems(command.userId(), itemIds);
        for (Long itemId : itemIdStockAmountMap.keySet()) {
            compensationActions.push(() -> cartService.putItem(buildCart(itemIdStockAmountMap, user, items, itemId)));
        }

        // 전체 코드에 트랜잭션 적용하면 분산락과 커넥션 간 데드락이 발생합니다.
        // 따라서 트랜잭셔널 메시징을 구현하기 위해 이벤트 발행만 하는 트랜잭션을 새로 시작합니다.
        // 이후 사가 패턴을 진행하면서 아래 코드는 주문을 저장하는 코드와 함께 합쳐질 예정입니다.
        orderService.publishOrderCreatedEvent(order.getId(), itemIdStockAmountMap);
        return order.getId();
    }

    private static Cart buildCart(Map<Long, Integer> itemIdStockAmountMap, User user, List<Item> items, Long itemId) {
        return Cart.builder()
                .user(user)
                .item(items.stream().filter(item -> item.getId().equals(itemId)).findFirst().orElseThrow())
                .quantity(itemIdStockAmountMap.get(itemId))
                .build();
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
