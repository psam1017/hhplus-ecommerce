package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.cart.CartInfo;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class CartFacade {

    public CartInfo.CartDetail putItem(Long userId, Long itemId, Integer amount) {
        return null;
    }

    public List<CartInfo.CartDetail> getCartItems(Long userId) {
        return null;
    }

    public CartInfo.CartDetail deleteCartItem(Long userId, Long itemId) {
        return null;
    }
}
