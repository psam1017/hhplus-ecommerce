package hhplus.ecommerce.server.interfaces.controller.cart;

import hhplus.ecommerce.server.domain.cart.CartInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CartDto {

    @Builder
    public record CartItemResponse(
            Long id,
            String name,
            Integer price,
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

    public record CartItemUpsertRequest(
            @NotNull Integer amount
    ) {
    }
}