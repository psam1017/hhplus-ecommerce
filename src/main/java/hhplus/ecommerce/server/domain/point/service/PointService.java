package hhplus.ecommerce.server.domain.point.service;

import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.exception.NoSuchPointException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class PointService {

    private final PointRepository pointRepository;

    public Point getPointByUserId(Long userId) {
        return pointRepository.findByUserId(userId).orElseThrow(NoSuchPointException::new);
    }

    public Point getPointByUserIdWithLock(Long userId) {
        return pointRepository.findByUserIdWithLock(userId).orElseThrow(NoSuchPointException::new);
    }

    public Point chargePoint(Long userId, Integer amount) {
        Point point = pointRepository.findByUserIdWithLock(userId).orElseThrow(NoSuchPointException::new);
        point.charge(amount);
        return point;
    }
}
