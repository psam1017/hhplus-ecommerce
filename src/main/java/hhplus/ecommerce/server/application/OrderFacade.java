package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.order.OrderInfo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderFacade {

    public Long doOrder(Long userId, Object post) {
        return null;
    }

    public List<OrderInfo.OrderDetail> getOrders(Long userId) {
        return null;
    }
}
