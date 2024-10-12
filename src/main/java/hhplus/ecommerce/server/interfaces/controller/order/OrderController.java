package hhplus.ecommerce.server.interfaces.controller.order;

import hhplus.ecommerce.server.application.OrderFacade;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}orders")
@RestController
public class OrderController {

    private final OrderFacade orderFacade;

    @PostMapping("")
    public ApiResponse<OrderDto.OrderIdResponse> doOrder(
            @PathVariable Long userId,
            @RequestBody @Valid OrderDto.OrderCreate post
    ) {
        return ApiResponse.ok(
                OrderDto.OrderIdResponse.builder()
                        .id(orderFacade.doOrder(userId, post))
                        .build()
        );
    }
}
