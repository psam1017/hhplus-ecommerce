package hhplus.ecommerce.server.infrastructure.event;

import hhplus.ecommerce.server.infrastructure.data.OrderDataPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OrderCreatedEventHandler {

    private final OrderDataPlatform orderDataPlatform;

    @Async
    @EventListener(OrderCreatedEventHandler.class)
    public void sendOrderDataToDataPlatform(OrderCreatedEvent event) {
        orderDataPlatform.saveOrderData(event.itemIdStockAmountMap());
    }
}
