package hhplus.ecommerce.server.interfaces.controller.order;

import hhplus.ecommerce.server.application.OrderFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "주문",
        description = "사용자의 주문에 대한 API"
)
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/orders")
@RestController
public class OrderController {

    private final OrderFacade orderFacade;

    @Operation(
            summary = "주문 생성",
            description = "특정 사용자의 주문을 생성합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자의 고유 식별자", required = true, in = ParameterIn.PATH)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "주문 생성 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = OrderDto.OrderCreate.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "생성된 주문의 ID",
                            content = @Content(
                                    schema = @Schema(implementation = OrderDto.OrderIdResponse.class)
                            )
                    )
            }
    )
    @PostMapping("")
    public OrderDto.OrderIdResponse doOrder(
            @PathVariable Long userId,
            @RequestBody @Valid OrderDto.OrderCreate post
    ) {
        return new OrderDto.OrderIdResponse(orderFacade.doOrder(userId, post));
    }

    @Operation(
            summary = "주문 목록 조회",
            description = "특정 사용자의 주문 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자의 고유 식별자", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "주문 목록",
                            content = @Content(
                                    schema = @Schema(implementation = OrderDto.OrderListResponse.class)
                            )
                    )
            }
    )
    @GetMapping("")
    public OrderDto.OrderListResponse getOrders(
            @PathVariable Long userId
    ) {
        return OrderDto.OrderListResponse.from(orderFacade.getOrders(userId));
    }

    @Operation(
            summary = "주문 조회",
            description = "특정 사용자의 주문을 조회합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자의 고유 식별자", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "orderId", description = "주문의 고유 식별자", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "주문 정보",
                            content = @Content(
                                    schema = @Schema(implementation = OrderDto.OrderAndItemResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/{orderId}")
    public OrderDto.OrderAndItemResponse getOrder(
            @PathVariable Long userId,
            @PathVariable Long orderId
    ) {
        return OrderDto.OrderAndItemResponse.from(orderFacade.getOrder(userId, orderId));
    }
}