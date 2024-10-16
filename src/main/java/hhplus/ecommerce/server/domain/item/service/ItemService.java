package hhplus.ecommerce.server.domain.item.service;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemException;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemStockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemStockRepository itemStockRepository;

    public List<Item> findTopItems() {
        LocalDateTime endDateTime = LocalDate.now().atStartOfDay();
        LocalDateTime startDateTime = endDateTime.minusDays(3);
        return itemRepository.findTopItems(startDateTime, endDateTime);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
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

    public void deductStocks(Map<Long, Integer> itemIdStockAmountMap) {
        Set<Long> itemIds = itemIdStockAmountMap.keySet();
        List<ItemStock> itemStocks = itemStockRepository.findAllByItemIdWithLock(itemIds);

        if (itemStocks.size() != itemIds.size()) {
            throw new NoSuchItemStockException();
        }
        itemStocks.forEach(stock -> stock.deductStock(itemIdStockAmountMap.get(stock.getItem().getId())));
    }
}
