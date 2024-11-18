package hhplus.ecommerce.server.domain.order.event;

import java.util.Map;

public record OrderCreatedEvent(
        Long orderId,
        Map<Long, Integer> itemIdStockAmountMap
) {
}
