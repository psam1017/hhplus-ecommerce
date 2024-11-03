package hhplus.ecommerce.server.domain.item.service;

import hhplus.ecommerce.server.domain.item.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ItemRepository {

    Optional<Item> findById(Long itemId);

    List<Item> findAllById(Set<Long> itemIds);

    List<Item> findAllBySearchCond(ItemCommand.ItemSearchCond searchCond);

    long countAllBySearchCond(ItemCommand.ItemSearchCond searchCond);

    List<Item> findTopItems(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
