package hhplus.ecommerce.server.infrastructure.cart;

import hhplus.ecommerce.server.domain.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<Cart, Long> {
}
