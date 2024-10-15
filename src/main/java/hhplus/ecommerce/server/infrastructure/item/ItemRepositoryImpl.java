package hhplus.ecommerce.server.infrastructure.item;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Transactional
@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private final ItemJpaRepository itemJpaRepository;

    @Override
    public List<Item> findTopItems(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return itemJpaRepository.findTopItemsOrderDateTimeBetween(startDateTime, endDateTime);
    }

    @Override
    public List<Item> findAll() {
        return itemJpaRepository.findAll();
    }

    @Override
    public List<Item> findAllById(Set<Long> itemIds) {
        return itemJpaRepository.findAllById(itemIds);
    }

    @Override
    public Optional<Item> findById(Long itemId) {
        return itemJpaRepository.findById(itemId);
    }
}
