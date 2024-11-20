package hhplus.ecommerce.server.infrastructure.platform;

import java.util.Map;

public class OrderDataPlatformMessage {

    public record OrderCreatedMessage(
            String transactionKey,
            Long orderId,
            Map<Long, Integer> itemIdStockAmountMap
    ) {
    }
}
