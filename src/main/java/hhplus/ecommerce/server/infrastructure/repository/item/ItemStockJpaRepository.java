package hhplus.ecommerce.server.infrastructure.repository.item;

import hhplus.ecommerce.server.domain.item.ItemStock;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ItemStockJpaRepository extends JpaRepository<ItemStock, Long> {
    Optional<ItemStock> findByItemId(Long itemId);

    List<ItemStock> findAllByItemIdIn(Set<Long> itemIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(
            @QueryHint(
                    name = "jakarta.persistence.lock.timeout",
                    value = "5000"
            )
    )
    @Query("""
            select i
            from ItemStock i
            where i.id = :id
            """)
    Optional<ItemStock> findByIdWithLock(Long id);
}
