package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.point.service.PointCommand;
import hhplus.ecommerce.server.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final PointService pointService;

    public Integer getPoint(Long userId) {
        return pointService.getPointByUserId(userId).getAmount();
    }

    public Integer chargePoint(Long userId, PointCommand.ChargePoint post) {
        return pointService.chargePoint(userId, post.chargeAmount()).getAmount();
    }
}
