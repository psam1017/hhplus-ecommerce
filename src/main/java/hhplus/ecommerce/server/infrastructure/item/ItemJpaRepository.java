package hhplus.ecommerce.server.infrastructure.item;

import hhplus.ecommerce.server.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemJpaRepository extends JpaRepository<Item, Long> {
}
