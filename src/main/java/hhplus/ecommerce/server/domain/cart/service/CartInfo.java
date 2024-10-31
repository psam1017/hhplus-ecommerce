package hhplus.ecommerce.server.domain.cart.service;

import hhplus.ecommerce.server.domain.cart.Cart;
import hhplus.ecommerce.server.domain.item.Item;

import java.util.List;

public class CartInfo {

    public record CartDetail(
            Long id,
            Long itemId,
            String name,
            Integer price,
            Integer amount,
            Integer leftStock
    ) {
        public static CartDetail from(Cart cart, Item item, int leftStock) {
            return new CartDetail(
                    cart.getId(),
                    item.getId(),
                    item.getName(),
                    item.getPrice(),
                    cart.getQuantity(),
                    leftStock
            );
        }
    }
}
