package hhplus.ecommerce.server.domain.cart.service;

import hhplus.ecommerce.server.domain.cart.Cart;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CartRepository {

    Optional<Cart> findByUserIdAndItemId(Long userId, Long itemId);

    Cart save(Cart cart);

    List<Cart> findAllByUserId(Long userId);

    void delete(Cart cart);

    void deleteByUserIdAndItemIds(Long userId, Set<Long> itemIds);

    void deleteAllById(Set<Long> cartIds);

    List<Cart> findAllByUserIdAndIdIn(Long userId, Set<Long> itemIds);
}
