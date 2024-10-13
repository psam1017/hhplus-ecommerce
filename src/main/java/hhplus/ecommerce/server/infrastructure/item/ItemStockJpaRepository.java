package hhplus.ecommerce.server.infrastructure.item;

import hhplus.ecommerce.server.domain.item.ItemStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemStockJpaRepository extends JpaRepository<ItemStock, Long> {
}
