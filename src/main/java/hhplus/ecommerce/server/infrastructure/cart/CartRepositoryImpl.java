package hhplus.ecommerce.server.infrastructure.cart;

import hhplus.ecommerce.server.domain.cart.Cart;
import hhplus.ecommerce.server.domain.cart.service.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Transactional
@Repository
public class CartRepositoryImpl implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    @Override
    public Optional<Cart> findByUserIdAndItemId(Long userId, Long itemId) {
        return cartJpaRepository.findByUserIdAndItemId(userId, itemId);
    }

    @Override
    public Cart save(Cart cart) {
        return cartJpaRepository.save(cart);
    }

    @Override
    public List<Cart> findAllByUserId(Long userId) {
        return cartJpaRepository.findAllByUserId(userId);
    }

    @Override
    public void delete(Cart cart) {
        cartJpaRepository.delete(cart);
    }

    @Override
    public void deleteByUserIdAndItemIds(Long userId, Set<Long> itemIds) {
        cartJpaRepository.deleteByUserIdAndItemIdIn(userId, itemIds);
    }

    @Override
    public void deleteAllById(Set<Long> cartIds) {
        cartJpaRepository.deleteAllById(cartIds);
    }

    @Override
    public List<Cart> findAllByUserIdAndIdIn(Long userId, Set<Long> cartIds) {
        return cartJpaRepository.findAllByUserIdAndIdIn(userId, cartIds);
    }
}
