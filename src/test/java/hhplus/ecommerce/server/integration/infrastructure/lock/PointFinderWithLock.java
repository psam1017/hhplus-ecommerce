package hhplus.ecommerce.server.integration.infrastructure.lock;

import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.infrastructure.lock.DistributedLock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class PointFinderWithLock {

    @Autowired
    EntityManager em;

    @Transactional
    public void findWithOptimisticLock(Long pointId, long millis) {
        em.find(Point.class, pointId, LockModeType.OPTIMISTIC);
        sleep(millis);
    }

    @Transactional
    public void findWithOptimisticLock(Long pointId) {
        em.find(Point.class, pointId, LockModeType.OPTIMISTIC);
    }

    @Transactional
    public void findWithPessimisticLock(Long pointId, long millis) {
        Map<String, Object> properties = Map.of("jakarta.persistence.lock.timeout", 500);
        em.find(Point.class, pointId, LockModeType.PESSIMISTIC_WRITE, properties);
        sleep(millis);
    }

    @Transactional
    public void findWithPessimisticLock(Long pointId) {
        Map<String, Object> properties = Map.of("jakarta.persistence.lock.timeout", 500);
        em.find(Point.class, pointId, LockModeType.PESSIMISTIC_WRITE, properties);
    }

    @DistributedLock(key = "'points:' + #pointId")
    public void findWithDistributionLock(Long pointId, long millis) {
        Map<String, Object> properties = Map.of("jakarta.persistence.lock.timeout", 500);
        em.find(Point.class, pointId, LockModeType.PESSIMISTIC_WRITE, properties);
        sleep(millis);
    }

    @DistributedLock(key = "'points:' + #pointId")
    public void findWithDistributionLock(Long pointId) {
        Map<String, Object> properties = Map.of("jakarta.persistence.lock.timeout", 500);
        em.find(Point.class, pointId, LockModeType.PESSIMISTIC_WRITE, properties);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
