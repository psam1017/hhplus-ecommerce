package hhplus.ecommerce.server.infrastructure.cart;

import hhplus.ecommerce.server.domain.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CartJpaRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserIdAndItemId(Long userId, Long itemId);

    List<Cart> findAllByUserId(Long userId);

    void deleteByUserIdAndItemIdIn(Long userId, Set<Long> itemIds);
}
