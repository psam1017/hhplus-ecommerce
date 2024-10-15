package hhplus.ecommerce.server.domain.item.service;

import hhplus.ecommerce.server.domain.item.Item;

import java.util.List;
import java.util.Map;

public class ItemInfo {

    public record ItemDetail(
            Long id,
            String name,
            Integer price,
            Integer amount
    ) {
        public static List<ItemDetail> from(List<Item> items, Map<Long, Integer> itemStocks) {
            return items.stream()
                    .map(item -> new ItemDetail(item.getId(), item.getName(), item.getPrice(), itemStocks.get(item.getId())))
                    .toList();
        }
    }
}
