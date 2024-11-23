package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.cart.Cart;
import hhplus.ecommerce.server.domain.cart.service.CartCommand;
import hhplus.ecommerce.server.domain.cart.service.CartInfo;
import hhplus.ecommerce.server.domain.cart.service.CartService;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CartFacade {

    private final UserService userService;
    private final ItemService itemService;
    private final CartService cartService;

    public CartInfo.CartDetail putItem(CartCommand.PutItem command) {
        ItemStock itemStock = itemService.getItemStockByItemId(command.itemId());
        itemStock.checkStock(command.amount());

        User user = userService.getUser(command.userId());
        Item item = itemService.getItem(command.itemId());
        return CartInfo.CartDetail.from(cartService.putItem(command.toCart(user, item)), item, itemStock.getAmount());
    }

    @Transactional(readOnly = true)
    public List<CartInfo.CartDetail> getCartItems(Long userId) {
        List<Cart> cartItems = cartService.getCartItems(userId);
        Set<Long> itemIds = cartItems.stream().map(c -> c.getItem().getId()).collect(Collectors.toSet());
        Map<Long, Integer> itemIdStockAmountMap = itemService.getStocks(itemIds);

        return cartItems.stream()
                .map(c -> {
                    Long itemId = c.getItem().getId();
                    return CartInfo.CartDetail.from(c, itemService.getItem(itemId), itemIdStockAmountMap.get(itemId));
                })
                .collect(Collectors.toList());
    }

    public Long deleteCartItem(Long userId, Long itemId) {
        return cartService.deleteCartItem(userId, itemId);
    }
}
