package hhplus.ecommerce.server.domain.point.service;

import hhplus.ecommerce.server.domain.point.Point;

import java.util.Optional;

public interface PointRepository {

    Optional<Point> findByUserId(Long userId);
}
