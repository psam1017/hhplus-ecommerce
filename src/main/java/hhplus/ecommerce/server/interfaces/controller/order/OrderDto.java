package hhplus.ecommerce.server.interfaces.controller.order;

import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.domain.order.service.OrderInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class OrderDto {

    public record OrderCreateFromItem(
            @NotEmpty @Valid
            @Schema(name = "items", description = "주문 항목 목록", example = "[{\"itemId\":101, \"amount\":2}]")
            List<OrderCreateItem> items
    ) {
    }

    public record OrderCreateItem(
            @NotNull
            @Schema(name = "itemId", description = "주문할 상품의 고유 식별자", example = "101")
            Long itemId,
            @Positive
            @NotNull
            @Schema(name = "amount", description = "주문할 상품의 수량", example = "2")
            Integer amount
    ) {
    }

    public record OrderCreateFromCart(
            @NotEmpty
            @Schema(name = "cartIds", description = "장바구니 ID 목록", example = "[101, 102]")
            Set<Long> cartIds
    ) {
    }

    public record OrderIdResponse(
            @Schema(name = "id", description = "주문 생성 결과 ID", example = "1001")
            Long id
    ) {
    }

    public record OrderResponse(
            @Schema(name = "id", description = "주문 ID", example = "1001")
            Long id,
            @Schema(name = "orderDateTime", description = "주문 일시", example = "2021-08-01T12:34:56")
            LocalDateTime orderDateTime,
            @Schema(name = "orderStatus", description = "주문 상태", example = "ORDERED")
            OrderStatus orderStatus,
            @Schema(name = "orderAmount", description = "주문 금액", example = "10000")
            Integer orderAmount
    ) {
    }

    public record OrderListResponse(
            @Schema(name = "orders", description = "주문 목록")
            List<OrderResponse> orders
    ) {
        public static OrderListResponse from(List<OrderInfo.OrderDetail> orders) {
            return new OrderListResponse(
                    orders.stream()
                            .map(order -> new OrderResponse(
                                    order.id(),
                                    order.orderDateTime(),
                                    order.status(),
                                    order.amount()
                            ))
                            .toList()
            );
        }
    }

    public record OrderItemResponse(
            @Schema(name = "id", description = "상품 ID", example = "101")
            Long id,
            @Schema(name = "name", description = "상품 이름", example = "상품 A")
            String name,
            @Schema(name = "price", description = "상품 가격", example = "5000")
            Integer price,
            @Schema(name = "quantity", description = "상품 수량", example = "2")
            Integer quantity
    ) {
    }

    public record OrderAndOrderItemsResponse(
            @Schema(name = "order", description = "주문 정보")
            OrderResponse order,
            @Schema(name = "items", description = "주문 상품 목록")
            List<OrderItemResponse> items
    ) {
        public static OrderAndOrderItemsResponse from(OrderInfo.OrderAndItemDetails orderAndItemDetails) {
            return new OrderAndOrderItemsResponse(
                    new OrderResponse(
                            orderAndItemDetails.orderDetail().id(),
                            orderAndItemDetails.orderDetail().orderDateTime(),
                            orderAndItemDetails.orderDetail().status(),
                            orderAndItemDetails.orderDetail().amount()
                    ),
                    orderAndItemDetails.orderItemDetails().stream()
                            .map(orderItemDetail -> new OrderItemResponse(
                                    orderItemDetail.id(),
                                    orderItemDetail.name(),
                                    orderItemDetail.price(),
                                    orderItemDetail.quantity()
                            ))
                            .toList()
            );
        }
    }
}
