package hhplus.ecommerce.server.infrastructure.platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class MockOrderDataPlatform implements OrderDataPlatform {

    private final ObjectMapper om;

    @Override
    public void saveOrderData(String value) {
        try {
            OrderDataPlatformMessage.OrderCreatedMessage message = om.readValue(value, OrderDataPlatformMessage.OrderCreatedMessage.class);
            log.info("Consumed order data: {}", message.itemIdStockAmountMap());
        } catch (JsonProcessingException e) {
            // some compensation, logging, alerting, email, etc.
            log.error("Failed to save order data", e);
        }
    }
}
