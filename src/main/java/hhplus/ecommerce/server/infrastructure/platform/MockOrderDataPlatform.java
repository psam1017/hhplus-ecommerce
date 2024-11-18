package hhplus.ecommerce.server.infrastructure.platform;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MockOrderDataPlatform implements OrderDataPlatform {

    @Override
    public void saveOrderData(Map<Long, Integer> itemIdItemAmountMap) {
        // TODO: 2024-10-15 implement this method
    }
}
