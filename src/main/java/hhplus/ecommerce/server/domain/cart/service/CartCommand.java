package hhplus.ecommerce.server.domain.cart.service;

import hhplus.ecommerce.server.domain.cart.Cart;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.user.User;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CartCommand {

    public record PutItem(
            Long userId,
            Long itemId,
            Integer amount
    ) {
        public Cart toCart(User user, Item item) {
            return Cart.builder()
                    .user(user)
                    .item(item)
                    .quantity(amount)
                    .build();
        }
    }
}
