package hhplus.ecommerce.server.infrastructure.repository.item;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemJpaCommandRepository extends JpaRepository<Item, Long> {

    @Modifying(flushAutomatically = true)
    @Query("""
            update Item i
            set i.status = :status
            where i.id = :id
            """)
    void modifyItemStatus(
            @Param("id") Long id,
            @Param("status") ItemStatus status
    );
}
