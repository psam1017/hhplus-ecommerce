package hhplus.ecommerce.server.interfaces.controller.cart;

import hhplus.ecommerce.server.application.CartFacade;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/carts")
@RestController
public class CartController {

    private final CartFacade cartFacade;

    @PutMapping("/{itemId}")
    public ApiResponse<CartDto.CartItemResponse> putItemIntoCart(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @RequestBody CartDto.CartItemUpsertRequest request
    ) {
        return ApiResponse.ok(
                CartDto.CartItemResponse.from(
                        cartFacade.putItem(userId, itemId, request.amount())
                )
        );
    }

    @GetMapping("")
    public ApiResponse<List<CartDto.CartItemResponse>> getCartItems(
            @PathVariable Long userId
    ) {
        return ApiResponse.ok(
                cartFacade.getCartItems(userId).stream()
                        .map(item -> CartDto.CartItemResponse.builder()
                                .id(item.id())
                                .name(item.name())
                                .price(item.price())
                                .amount(item.amount())
                                .build()
                        )
                        .toList()
        );
    }

    @DeleteMapping("/{itemId}")
    public ApiResponse<String> deleteItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long itemId
    ) {
        cartFacade.deleteCartItem(userId, itemId);
        return ApiResponse.ok("OK");
    }
}