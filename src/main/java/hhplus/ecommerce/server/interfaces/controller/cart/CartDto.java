package hhplus.ecommerce.server.interfaces.controller.cart;

import hhplus.ecommerce.server.domain.cart.service.CartInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.experimental.UtilityClass;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@UtilityClass
public class CartDto {

    @Builder
    public record CartItemResponse(
            @Schema(name = "id", description = "장바구니 항목의 고유 식별자", example = "101")
            Long id,

            @Schema(name = "name", description = "상품의 이름", example = "사과")
            String name,

            @Schema(name = "price", description = "상품의 가격", example = "1000")
            Integer price,

            @Schema(name = "amount", description = "상품의 수량", example = "1")
            Integer amount
    ) {
        public static CartItemResponse from(CartInfo.CartDetail cartDetail) {
            return new CartItemResponse(
                    cartDetail.id(),
                    cartDetail.name(),
                    cartDetail.price(),
                    cartDetail.amount()
            );
        }
    }

    public record CartItemResponseList(
            @Schema(name = "items", description = "장바구니 항목 목록", example = "[{\"id\":101, \"name\":\"사과\", \"price\":1000, \"amount\":3}]")
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
            @NotNull
            @Schema(name = "amount", description = "상품의 수량", example = "1")
            Integer amount
    ) {
    }

    public record CartItemDeleteResponse(
            @Schema(name = "id", description = "장바구니 항목의 고유 식별자", example = "101")
            Long id
    ) {
        public static CartItemDeleteResponse from(CartInfo.CartDetail cartDetail) {
            return new CartItemDeleteResponse(
                    cartDetail.id()
            );
        }
    }
}