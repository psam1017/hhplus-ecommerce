package hhplus.ecommerce.server.infrastructure.platform;

import java.util.Map;

public interface OrderDataPlatform {

    void saveOrderData(Map<Long, Integer> itemIdItemAmountMap);
}
