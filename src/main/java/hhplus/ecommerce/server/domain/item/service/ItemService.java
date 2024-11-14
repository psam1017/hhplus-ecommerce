package hhplus.ecommerce.server.domain.item.service;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemException;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemStockException;
import hhplus.ecommerce.server.infrastructure.cache.CacheName;
import hhplus.ecommerce.server.infrastructure.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(
            cacheNames = CacheName.ITEMS_TOP,
            key = "T(java.time.LocalDate).now().toString()"
    )
    public List<Item> findTopItems() {
        LocalDateTime endDateTime = LocalDate.now().atStartOfDay();
        LocalDateTime startDateTime = endDateTime.minusDays(3);
        return itemRepository.findTopItems(startDateTime, endDateTime);
    }

    @Cacheable(
            cacheNames = CacheName.ITEMS_PAGE,
            condition = """
                    #searchCond.page() < 100
                    && #searchCond.size() == 10
                    && T(java.util.List).of("id", "price").contains(#searchCond.prop())
                    && T(java.util.List).of("asc", "desc").contains(#searchCond.dir())
                    && (#searchCond.keyword() == null || #searchCond.keyword().isBlank())
                    """,
            key = "T(String).format('page:%d:size:%d:prop:%s:dir:%s', #searchCond.page(), #searchCond.size(), #searchCond.prop(), #searchCond.dir())"
    )
    public List<Item> findItemsBySearchCond(ItemCommand.ItemSearchCond searchCond) {
        return itemRepository.findAllBySearchCond(searchCond);
    }

    @Cacheable(
            cacheNames = CacheName.ITEMS_PAGE,
            condition = "#searchCond.keyword() == null || #searchCond.keyword().isBlank()",
            key = "'count'"
    )
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
