package hhplus.ecommerce.server.interfaces.controller.order;

import hhplus.ecommerce.server.domain.order.OrderInfo;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.interfaces.common.jsonformat.KoreanDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.List;

@UtilityClass
public class OrderDto {

    public record OrderCreate(
            @NotEmpty @Valid
            @Schema(name = "items", description = "주문 항목 목록", example = "[{\"itemId\":101, \"amount\":2}]")
            List<OrderCreateItem> items
    ) {
    }

    public record OrderCreateItem(
            @NotNull
            @Schema(name = "itemId", description = "주문할 상품의 고유 식별자", example = "101")
            Long itemId,

            @NotNull
            @Schema(name = "amount", description = "주문할 상품의 수량", example = "2")
            Integer amount
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
            @KoreanDateTime
            LocalDateTime orderDateTime,
            @Schema(name = "orderTitle", description = "주문 제목", example = "상품 A 외 1건")
            String orderTitle,
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
                                    order.title(),
                                    order.status(),
                                    order.amount()
                            ))
                            .toList()
            );
        }
    }
}
