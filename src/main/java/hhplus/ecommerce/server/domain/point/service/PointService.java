package hhplus.ecommerce.server.domain.point.service;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.exception.NoSuchPointException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Transactional
@Service
public class PointService {

    private final PointRepository pointRepository;

    public Point getPointByUserId(Long userId) {
        return pointRepository.findByUserId(userId).orElseThrow(NoSuchPointException::new);
    }

    public Point chargePoint(Long userId, Integer amount) {
        Point point = pointRepository.findByUserIdWithLock(userId).orElseThrow(NoSuchPointException::new);
        point.charge(amount);
        return point;
    }

    public void usePoint(Long userId, List<Item> items, Map<Long, Integer> itemIdStockAmountMap) {
        Point point = pointRepository.findByUserIdWithLock(userId).orElseThrow(NoSuchPointException::new);
        int totalPrice = calculateTotalPrice(itemIdStockAmountMap, items);
        point.usePoint(totalPrice);
    }

    private static int calculateTotalPrice(Map<Long, Integer> itemIdStockAmountMap, List<Item> items) {
        int totalPrice = 0;
        for (Item item : items) {
            totalPrice += item.getPrice() * itemIdStockAmountMap.get(item.getId());
        }
        return totalPrice;
    }
}
