package hhplus.ecommerce.server.domain.order.message;

import java.util.Map;

public record OrderCreatedEvent(
        String transactionKey,
        Long orderId,
        Map<Long, Integer> itemIdStockAmountMap
) {
}
