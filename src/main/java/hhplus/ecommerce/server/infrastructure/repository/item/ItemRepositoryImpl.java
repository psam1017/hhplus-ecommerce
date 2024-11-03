package hhplus.ecommerce.server.infrastructure.repository.item;

import hhplus.ecommerce.server.domain.item.Item;
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

    private final ItemJpaQueryRepository itemJpaCommandRepository;

    @Override
    public Optional<Item> findById(Long itemId) {
        return itemJpaCommandRepository.findById(itemId);
    }

    @Override
    public List<Item> findAllById(Set<Long> itemIds) {
        return itemJpaCommandRepository.findAllById(itemIds);
    }

    @Override
    public List<Item> findAllBySearchCond(ItemCommand.ItemSearchCond searchCond) {
        return itemJpaCommandRepository.findAllBySearchCond(searchCond);
    }

    @Override
    public long countAllBySearchCond(ItemCommand.ItemSearchCond searchCond) {
        return itemJpaCommandRepository.countAllBySearchCond(searchCond);
    }

    @Override
    public List<Item> findTopItems(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return itemJpaCommandRepository.findTopItemsOrderDateTimeBetween(startDateTime, endDateTime);
    }
}
