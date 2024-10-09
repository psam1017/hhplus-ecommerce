package hhplus.ecommerce.server.interfaces.web.order;

import hhplus.ecommerce.server.application.common.model.slicing.SlicingInfo;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.interfaces.web.order.model.request.OrderPost;
import hhplus.ecommerce.server.interfaces.web.order.model.request.OrderSlicingSearchCond;
import hhplus.ecommerce.server.interfaces.web.order.model.response.OrderDetail;
import hhplus.ecommerce.server.interfaces.web.order.model.response.OrderIdResponse;
import hhplus.ecommerce.server.interfaces.web.order.model.response.OrderSlicingResult;
import hhplus.ecommerce.server.interfaces.web.order.model.response.OrderSummary;
import hhplus.ecommerce.server.interfaces.web.support.argument.UserId;
import hhplus.ecommerce.server.interfaces.web.support.model.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/api/members/orders")
@RestController
public class MemberOrderController {

    @PostMapping("/new")
    public ApiResponse<OrderIdResponse> doOrder(
            @UserId Long userId,
            @RequestBody @Valid OrderPost post
    ) {
        return ApiResponse.ok(OrderIdResponse.builder()
                .orderId(1L)
                .build());
    }

    @PostMapping("/{orderId}/confirm")
    public ApiResponse<?> confirmOrder(
            @UserId Long userId,
            @PathVariable Long orderId
    ) {
        return ApiResponse.ok(OrderIdResponse.builder()
                .orderId(orderId)
                .build());
    }

    @Operation(
            parameters = {
                    @Parameter(name = "size"),
                    @Parameter(name = "lastSequence"),
                    @Parameter(name = "searchStartDateTime"),
                    @Parameter(name = "searchEndDateTime")
            }
    )
    @GetMapping("")
    public ApiResponse<OrderSlicingResult> getOrders(
            @UserId Long userId,
            @Parameter(hidden = true) @ModelAttribute OrderSlicingSearchCond cond
    ) {
        List<OrderSummary> orders = List.of(
                OrderSummary.builder()
                        .id(1L)
                        .title("주문1")
                        .orderDateTime(LocalDateTime.now())
                        .orderStatus(OrderStatus.ORDERED)
                        .orderStatusValue(OrderStatus.ORDERED.value())
                        .build()
        );
        OrderSlicingResult result = OrderSlicingResult.builder()
                .orders(orders)
                .slicingInfo(new SlicingInfo(10, 120L, true))
                .build();
        return ApiResponse.ok(result);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetail> getOrderDetail(
            @UserId Long userId,
            @PathVariable Long orderId
    ) {
        OrderDetail.OrderDetailItem item = OrderDetail.OrderDetailItem.builder()
                .orderItemId(1L)
                .itemId(1L)
                .name("상품1")
                .price(1000)
                .amount(1)
                .build();
        OrderDetail order = OrderDetail.builder()
                .id(orderId)
                .title("주문1")
                .orderDateTime(LocalDateTime.now())
                .orderStatus(OrderStatus.ORDERED)
                .orderStatusValue(OrderStatus.ORDERED.value())
                .orderItems(List.of(item))
                .build();

        return ApiResponse.ok(order);
    }
}
