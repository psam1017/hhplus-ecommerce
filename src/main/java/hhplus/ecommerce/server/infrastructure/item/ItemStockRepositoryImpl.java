package hhplus.ecommerce.server.infrastructure.item;

import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.service.ItemStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Transactional
@Repository
public class ItemStockRepositoryImpl implements ItemStockRepository {

    private final ItemStockJpaRepository itemStockJpaRepository;

    @Override
    public List<ItemStock> findAllByItemIds(Set<Long> itemIds) {
        return itemStockJpaRepository.findAllByItemIdIn(itemIds);
    }

    @Override
    public Optional<ItemStock> findByItemId(Long itemId) {
        return itemStockJpaRepository.findByItemId(itemId);
    }

    @Override
    public Optional<ItemStock> findByItemIdWithLock(Long itemId) {
        return itemStockJpaRepository.findByItemIdWithLock(itemId);
    }
}
