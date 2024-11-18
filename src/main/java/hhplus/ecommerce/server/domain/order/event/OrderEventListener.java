package hhplus.ecommerce.server.domain.order.event;

import hhplus.ecommerce.server.infrastructure.platform.OrderDataPlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static hhplus.ecommerce.server.config.AsyncConfig.EVENT_ASYNC_TASK_EXECUTOR;

@RequiredArgsConstructor
@Component
public class OrderEventListener {

    private final OrderDataPlatform orderDataPlatform;

    @Async(EVENT_ASYNC_TASK_EXECUTOR)
    @EventListener(OrderEventListener.class)
    public void sendOrderDataToDataPlatform(OrderCreatedEvent event) {
        orderDataPlatform.saveOrderData(event.itemIdStockAmountMap());
    }
}
