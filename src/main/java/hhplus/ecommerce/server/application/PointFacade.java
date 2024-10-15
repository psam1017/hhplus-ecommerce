package hhplus.ecommerce.server.application;

import hhplus.ecommerce.server.domain.point.service.PointService;
import hhplus.ecommerce.server.interfaces.controller.point.PointDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final PointService pointService;

    public Integer getPoint(Long userId) {
        return pointService.getPointByUserId(userId).getAmount();
    }

    public Integer chargePoint(Long userId, PointDto.PointCreate post) {
        return pointService.chargePoint(userId, post.chargeAmount()).getAmount();
    }
}
