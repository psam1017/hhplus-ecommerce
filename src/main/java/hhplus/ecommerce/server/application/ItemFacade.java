package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
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

    public ItemInfo.ItemPageInfo pageItems(ItemCommand.ItemSearchCond searchCond) {
        List<Item> items = itemService.findItemsBySearchCond(searchCond);
        long count = itemService.countItemsBySearchCond(searchCond, items.size());
        Map<Long, Integer> stockMap = itemService.getStocks(items.stream().map(Item::getId).collect(Collectors.toSet()));
        List<ItemInfo.ItemDetail> itemDetails = ItemInfo.ItemDetail.from(items, stockMap);
        return ItemInfo.ItemPageInfo.from(itemDetails, count);
    }
}
