package hhplus.ecommerce.server.interfaces.controller.cart;

import hhplus.ecommerce.server.application.CartFacade;
import hhplus.ecommerce.server.domain.cart.service.CartCommand;
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
        name = "장바구니",
        description = "사용자의 장바구니에 대한 API"
)
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/carts")
@RestController
public class CartController {

    private final CartFacade cartFacade;

    @Operation(
            summary = "장바구니에 항목 추가 또는 업데이트",
            description = "특정 사용자 장바구니에 항목을 추가하거나 업데이트합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자의 고유 식별자", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "itemId", description = "항목의 고유 식별자", required = true, in = ParameterIn.PATH)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업데이트할 장바구니 항목 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CartDto.CartItemPut.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "업데이트된 장바구니 항목",
                            content = @Content(
                                    schema = @Schema(implementation = CartDto.CartItemResponse.class)
                            )
                    )
            }
    )
    @PutMapping("/{itemId}")
    public CartDto.CartItemResponse putItemIntoCart(
            @PathVariable Long userId,
            @PathVariable Long itemId,
            @RequestBody @Valid CartDto.CartItemPut request
    ) {
        return CartDto.CartItemResponse.from(cartFacade.putItem(new CartCommand.PutItem(userId, itemId, request.amount())));
    }

    @Operation(
            summary = "장바구니 항목 조회",
            description = "특정 사용자의 장바구니에 있는 모든 항목을 조회합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자의 고유 식별자", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "장바구니 항목 목록",
                            content = @Content(
                                    schema = @Schema(implementation = CartDto.CartItemResponseList.class)
                            )
                    )
            }
    )
    @GetMapping("")
    public CartDto.CartItemResponseList getCartItems(
            @PathVariable Long userId
    ) {
        return CartDto.CartItemResponseList.from(cartFacade.getCartItems(userId));
    }

    @Operation(
            summary = "장바구니 항목 삭제",
            description = "특정 사용자의 장바구니에서 항목을 삭제합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자의 고유 식별자", required = true, in = ParameterIn.PATH),
                    @Parameter(name = "itemId", description = "삭제할 항목의 고유 식별자", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공적으로 삭제됨",
                            content = @Content(
                                    schema = @Schema(implementation = CartDto.CartItemDeleteResponse.class)
                            )
                    )
            }
    )
    @DeleteMapping("/{itemId}")
    public CartDto.CartItemDeleteResponse deleteItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long itemId
    ) {
        return CartDto.CartItemDeleteResponse.from(
                cartFacade.deleteCartItem(userId, itemId)
        );
    }
}