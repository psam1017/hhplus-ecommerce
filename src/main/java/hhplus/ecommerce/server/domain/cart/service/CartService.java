package hhplus.ecommerce.server.domain.cart.service;

import hhplus.ecommerce.server.domain.cart.Cart;
import hhplus.ecommerce.server.domain.cart.exception.NoSuchCartException;
import hhplus.ecommerce.server.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Transactional
@Service
public class CartService {

    private final CartRepository cartRepository;

    public Cart putItem(Cart cart) {
        Optional<Cart> optCart = cartRepository.findByUserIdAndItemId(cart.getUser().getId(), cart.getItem().getId());
        if (optCart.isPresent()) {
            Cart existCart = optCart.get();
            existCart.putQuantity(cart.getQuantity());
            return existCart;
        }
        return cartRepository.save(cart);
    }

    public List<Cart> getCartItems(Long userId) {
        return cartRepository.findAllByUserId(userId);
    }

    public Long deleteCartItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByUserIdAndItemId(userId, itemId).orElseThrow(NoSuchCartException::new);
        cartRepository.delete(cart);
        return cart.getId();
    }

    public void deleteCartItems(Long userId, Set<Long> itemIds) {
        cartRepository.deleteByUserIdAndItemIds(userId, itemIds);
    }
}
