package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemInfo;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ItemFacade {

    private final ItemService itemService;

    public List<ItemInfo.ItemDetail> findTopItems() {
        List<Item> topItems = itemService.findTopItems();
        Map<Long, Integer> stockMap = itemService.getStocks(topItems.stream().map(Item::getId).collect(Collectors.toSet()));
        return ItemInfo.ItemDetail.from(topItems, stockMap);
    }

    public List<ItemInfo.ItemDetail> findItems() {
        List<Item> items = itemService.findItems();
        Map<Long, Integer> stockMap = itemService.getStocks(items.stream().map(Item::getId).collect(Collectors.toSet()));
        return ItemInfo.ItemDetail.from(items, stockMap);
    }
}
