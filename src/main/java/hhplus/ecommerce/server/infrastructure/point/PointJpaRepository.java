package hhplus.ecommerce.server.infrastructure.point;

import hhplus.ecommerce.server.domain.point.Point;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<Point, Long> {
    Optional<Point> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(
            @QueryHint(
                    name = "jakarta.persistence.lock.timeout",
                    value = "5000"
            )
    )
    @Query("""
            select p
            from Point p
            where p.user.id = :userId
            """)
    Optional<Point> findByUserIdWithLock(Long userId);
}
