package hhplus.ecommerce.server.infrastructure.event;

import java.util.Map;

public record OrderCreatedEvent(
        Long orderId,
        Map<Long, Integer> itemIdStockAmountMap
) {
}
