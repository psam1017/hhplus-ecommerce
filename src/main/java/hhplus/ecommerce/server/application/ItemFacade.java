package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.item.service.ItemInfo;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ItemFacade {

    private final OrderService orderService;
    private final ItemService itemService;

    public List<ItemInfo.ItemDetail> findTopItems(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Long> topItemIds = orderService.findTopItemIds(startDateTime, endDateTime);
        List<Item> items = itemService.findItemsInSameOrder(topItemIds);
        Map<Long, Integer> stockMap = itemService.getStocks(items.stream().map(Item::getId).collect(Collectors.toSet()));
        return ItemInfo.ItemDetail.from(items, stockMap);
    }

    public ItemInfo.ItemPageInfo pageItems(ItemCommand.ItemSearchCond searchCond) {
        List<Item> items = itemService.findItemsBySearchCond(searchCond);
        long count = itemService.countItemsBySearchCond(searchCond, items.size());
        Map<Long, Integer> stockMap = itemService.getStocks(items.stream().map(Item::getId).collect(Collectors.toSet()));
        List<ItemInfo.ItemDetail> itemDetails = ItemInfo.ItemDetail.from(items, stockMap);
        return ItemInfo.ItemPageInfo.from(itemDetails, count);
    }
}
