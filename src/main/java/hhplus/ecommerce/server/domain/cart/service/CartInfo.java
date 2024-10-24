package hhplus.ecommerce.server.domain.cart.service;

import hhplus.ecommerce.server.domain.cart.Cart;

import java.util.List;

public class CartInfo {

    public record CartDetail(
            Long id,
            Long itemId,
            String name,
            Integer price,
            Integer amount
    ) {
        public static CartDetail from(Cart cart) {
            return new CartDetail(
                    cart.getId(),
                    cart.getItem().getId(),
                    cart.getItem().getName(),
                    cart.getItem().getPrice(),
                    cart.getQuantity()
            );
        }
    }
}
