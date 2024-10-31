package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.service.PointCommand;
import hhplus.ecommerce.server.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final PointService pointService;

    public Integer getPoint(Long userId) {
        return pointService.getPointByUserId(userId).getAmount();
    }

    public Integer chargePoint(Long userId, PointCommand.ChargePoint post) {
        Point point = pointService.getPointByUserId(userId);
        return pointService.chargePoint(point.getId(), post.chargeAmount()).getAmount();
    }
}
