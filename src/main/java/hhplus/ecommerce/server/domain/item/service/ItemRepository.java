package hhplus.ecommerce.server.domain.item.service;

import hhplus.ecommerce.server.domain.item.Item;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ItemRepository {

    List<Item> findTopItems(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<Item> findAll();

    List<Item> findAllById(Set<Long> itemIds);

    Optional<Item> findById(Long itemId);
}
