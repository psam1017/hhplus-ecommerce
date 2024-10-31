package hhplus.ecommerce.server.domain.item.service;

import hhplus.ecommerce.server.domain.item.ItemStock;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ItemStockRepository {

    List<ItemStock> findAllByItemIds(Set<Long> itemIds);

    Optional<ItemStock> findByItemId(Long itemId);

    Optional<ItemStock> findByIdWithLock(Long id);
}
