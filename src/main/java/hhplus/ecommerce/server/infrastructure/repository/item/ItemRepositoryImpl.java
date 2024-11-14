package hhplus.ecommerce.server.infrastructure.repository.item;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStatus;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
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

    private final ItemJpaCommandRepository itemJpaCommandRepository;
    private final ItemJpaQueryRepository itemJpaQueryRepository;

    @Override
    public Optional<Item> findById(Long itemId) {
        return itemJpaQueryRepository.findById(itemId);
    }

    @Override
    public List<Item> findAllById(Set<Long> itemIds) {
        return itemJpaQueryRepository.findAllById(itemIds);
    }

    @Override
    public List<Item> findAllBySearchCond(ItemCommand.ItemSearchCond searchCond) {
        return itemJpaQueryRepository.findAllBySearchCond(searchCond);
    }

    @Override
    public long countAllBySearchCond(ItemCommand.ItemSearchCond searchCond) {
        return itemJpaQueryRepository.countAllBySearchCond(searchCond);
    }

    @Override
    public List<Item> findTopItems(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return itemJpaQueryRepository.findTopItemsOrderDateTimeBetween(startDateTime, endDateTime);
    }

    @Override
    public void modifyItemStatus(Long id, ItemStatus status) {
        itemJpaCommandRepository.modifyItemStatus(id, status);
    }
}
