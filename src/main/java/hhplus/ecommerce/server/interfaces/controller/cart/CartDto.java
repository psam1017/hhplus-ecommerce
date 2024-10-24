package hhplus.ecommerce.server.interfaces.controller.cart;

import hhplus.ecommerce.server.domain.cart.service.CartInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.List;

public class CartDto {

    @Builder
    public record CartItemResponse(
            @Schema(name = "id", description = "장바구니 항목의 고유 식별자", example = "101")
            Long id,
            @Schema(name = "itemId", description = "상품의 고유 식별자", example = "201")
            Long itemId,

            @Schema(name = "name", description = "상품의 이름", example = "사과")
            String name,

            @Schema(name = "price", description = "상품의 가격", example = "1000")
            Integer price,

            @Schema(name = "amount", description = "상품의 수량", example = "1")
            Integer amount,
            @Schema(name = "leftStock", description = "상품의 재고", example = "10")
            Integer leftStock
    ) {
        public static CartItemResponse from(CartInfo.CartDetail cartDetail) {
            return new CartItemResponse(
                    cartDetail.id(),
                    cartDetail.itemId(),
                    cartDetail.name(),
                    cartDetail.price(),
                    cartDetail.amount(),
                    cartDetail.leftStock()
            );
        }
    }

    public record CartItemResponseList(
            @Schema(name = "items", description = "장바구니 항목 목록", example = "[{\"id\":101, \"itemId\":201, \"name\":\"사과\", \"price\":1000, \"amount\":3}]")
            List<CartItemResponse> items
    ) {
        public static CartItemResponseList from(List<CartInfo.CartDetail> cartDetails) {
            return new CartItemResponseList(
                    cartDetails.stream()
                            .map(CartItemResponse::from)
                            .toList()
            );
        }
    }

    public record CartItemPut(
            @Positive
            @NotNull
            @Schema(name = "amount", description = "상품의 수량", example = "1")
            Integer amount
    ) {
    }

    public record CartItemDeleteResponse(
            @Schema(name = "id", description = "장바구니 항목의 고유 식별자", example = "101")
            Long id
    ) {
        public static CartItemDeleteResponse from(Long cartId) {
            return new CartItemDeleteResponse(cartId);
        }
    }
}