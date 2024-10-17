package hhplus.ecommerce.server.infrastructure.data;

import java.util.Map;

public interface OrderDataPlatform {

    void saveOrderData(Map<Long, Integer> itemIdItemAmountMap);
}
