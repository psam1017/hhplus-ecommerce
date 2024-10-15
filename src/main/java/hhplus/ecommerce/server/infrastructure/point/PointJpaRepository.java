package hhplus.ecommerce.server.infrastructure.point;

import hhplus.ecommerce.server.domain.point.Point;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from Point p
            where p.user.id = :userId
            """)
    Optional<Point> findByUserIdWithLock(Long userId);
}
