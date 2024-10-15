package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.cart.service.CartCommand;
import hhplus.ecommerce.server.domain.cart.service.CartInfo;
import hhplus.ecommerce.server.domain.cart.service.CartService;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CartFacade {

    private final UserService userService;
    private final ItemService itemService;
    private final CartService cartService;

    public CartInfo.CartDetail putItem(CartCommand.PutItem command) {
        ItemStock itemStock = itemService.getItemStockByItemId(command.itemId());
        itemStock.checkStock(command.amount());

        User user = userService.getUser(command.userId());
        Item item = itemService.getItem(command.itemId());
        return CartInfo.CartDetail.from(cartService.putItem(command.toCart(user, item)));
    }

    public List<CartInfo.CartDetail> getCartItems(Long userId) {
        return cartService.getCartItems(userId).stream()
                .map(CartInfo.CartDetail::from)
                .toList();
    }

    public Long deleteCartItem(Long userId, Long itemId) {
        return cartService.deleteCartItem(userId, itemId);
    }
}
