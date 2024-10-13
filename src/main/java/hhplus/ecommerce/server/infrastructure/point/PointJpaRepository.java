package hhplus.ecommerce.server.infrastructure.point;

import hhplus.ecommerce.server.domain.point.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointJpaRepository extends JpaRepository<Point, Long> {
}
