package hhplus.ecommerce.server.domain.item.service;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemException;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemStockException;
import hhplus.ecommerce.server.infrastructure.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;

    public List<Item> findTopItems() {
        LocalDateTime endDateTime = LocalDate.now().atStartOfDay();
        LocalDateTime startDateTime = endDateTime.minusDays(3);
        return itemRepository.findTopItems(startDateTime, endDateTime);
    }

    public List<Item> findItemsBySearchCond(ItemCommand.ItemSearchCond searchCond) {
        return itemRepository.findAllBySearchCond(searchCond);
    }

    public long countItemsBySearchCond(ItemCommand.ItemSearchCond searchCond, int contentSize) {
        if (searchCond.size() > contentSize) {
            if (searchCond.getOffset() == 0 || contentSize != 0) {
                return searchCond.getOffset() + contentSize;
            }
        }
        return itemRepository.countAllBySearchCond(searchCond);
    }

    public List<Item> findItems(Set<Long> itemIds) {
        List<Item> items = itemRepository.findAllById(itemIds);
        if (items.size() != itemIds.size()) {
            throw new NoSuchItemException();
        }
        return items;
    }

    public Item getItem(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(NoSuchItemException::new);
    }

    public Map<Long, Integer> getStocks(Set<Long> itemIds) {
        List<ItemStock> itemStocks = itemStockRepository.findAllByItemIds(itemIds);
        if (itemStocks.size() != itemIds.size()) {
            throw new NoSuchItemStockException();
        }
        return itemStocks.stream()
                .collect(Collectors.toMap(
                        itemStock -> itemStock.getItem().getId(),
                        ItemStock::getAmount
                ));
    }

    public ItemStock getItemStockByItemId(Long itemId) {
        return itemStockRepository.findByItemId(itemId).orElseThrow(NoSuchItemStockException::new);
    }

    @DistributedLock(key = "'item_stocks:' + #itemStockId")
    public void deductStock(Long itemStockId, int amount) {
        ItemStock itemStock = itemStockRepository.findByIdWithLock(itemStockId).orElseThrow(NoSuchItemStockException::new);
        itemStock.deductStock(amount);
    }

    @DistributedLock(key = "'item_stocks:' + #itemStockId")
    public void restoreStock(Long itemStockId, int amount) {
        ItemStock itemStock = itemStockRepository.findByIdWithLock(itemStockId).orElseThrow(NoSuchItemStockException::new);
        itemStock.addStock(amount);
    }
}
