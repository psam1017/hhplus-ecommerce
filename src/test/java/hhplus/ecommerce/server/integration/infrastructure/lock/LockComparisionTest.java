package hhplus.ecommerce.server.integration.infrastructure.lock;

import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.repository.point.PointJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.user.UserJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

public class LockComparisionTest extends TestContainerEnvironment {

    @Autowired
    EntityManager em;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    PointJpaRepository pointJpaRepository;

    @Autowired
    PointFinderWithLock pointFinder;

    @DisplayName("동시 요청 횟수가 적을 때 락의 처리속도는 '낙관적 락 < 비관적 락 ~= 분산락' 으로 측정된다.")
    @Test
    void compareLockWithFewRequest() throws InterruptedException {
        // given
        int tryCount = 5;
        long millis = 100;
        Point point = createPoint();

        // when 1 - 낙관적 락
        ExecutorService executorService = Executors.newFixedThreadPool(tryCount);
        CountDownLatch startLatch1 = new CountDownLatch(1);
        CountDownLatch endLatch1 = new CountDownLatch(tryCount);

        for (int i = 0; i < tryCount; i++) {
            executorService.execute(() -> {
                try {
                    startLatch1.await();
                    pointFinder.findWithOptimisticLock(point.getId(), millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch1.countDown();
                }
            });
        }

        long startMillis = System.currentTimeMillis();
        startLatch1.countDown();
        endLatch1.await();
        long optimisticLockDuration = System.currentTimeMillis() - startMillis;

        // when 2 - 비관적 락
        CountDownLatch startLatch2 = new CountDownLatch(1);
        CountDownLatch endLatch2 = new CountDownLatch(tryCount);

        for (int i = 0; i < tryCount; i++) {
            executorService.execute(() -> {
                try {
                    startLatch2.await();
                    pointFinder.findWithPessimisticLock(point.getId(), millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch2.countDown();
                }
            });
        }

        startMillis = System.currentTimeMillis();
        startLatch2.countDown();
        endLatch2.await();
        long pessimisticLockDuration = System.currentTimeMillis() - startMillis;

        // when 3 - 분산락
        CountDownLatch startLatch3 = new CountDownLatch(1);
        CountDownLatch endLatch3 = new CountDownLatch(tryCount);

        for (int i = 0; i < tryCount; i++) {
            executorService.execute(() -> {
                try {
                    startLatch3.await();
                    pointFinder.findWithDistributionLock(point.getId(), millis);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch3.countDown();
                }
            });
        }

        startMillis = System.currentTimeMillis();
        startLatch3.countDown();
        endLatch3.await();
        long distributionLockDuration = System.currentTimeMillis() - startMillis;

        // then
        assertThat(optimisticLockDuration).isLessThan(pessimisticLockDuration);
        assertThat(optimisticLockDuration).isLessThan(distributionLockDuration);
    }

    @DisplayName("동시 요청 횟수가 적을 때 락의 처리속도는 '낙관적 락 < 비관적 락 < 분산락' 으로 측정된다.")
    @Test
    void compareLock() throws InterruptedException {
        // given
        int tryCount = 200;
        int repeatCount = 5;
        Point point = createPoint();

        // when 1 - 낙관적 락
        ExecutorService executorService = Executors.newFixedThreadPool(tryCount);
        CountDownLatch startLatch1 = new CountDownLatch(1);
        CountDownLatch endLatch1 = new CountDownLatch(tryCount);

        for (int i = 0; i < tryCount; i++) {
            executorService.execute(() -> {
                try {
                    startLatch1.await();
                    for (int j = 0; j < repeatCount; j++) {
                        pointFinder.findWithOptimisticLock(point.getId());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch1.countDown();
                }
            });
        }

        long startMillis = System.currentTimeMillis();
        startLatch1.countDown();
        endLatch1.await();
        long optimisticLockDuration = System.currentTimeMillis() - startMillis;

        // when 2 - 비관적 락
        CountDownLatch startLatch2 = new CountDownLatch(1);
        CountDownLatch endLatch2 = new CountDownLatch(tryCount);

        for (int i = 0; i < tryCount; i++) {
            executorService.execute(() -> {
                try {
                    startLatch2.await();
                    for (int j = 0; j < repeatCount; j++) {
                        pointFinder.findWithPessimisticLock(point.getId());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch2.countDown();
                }
            });
        }

        startMillis = System.currentTimeMillis();
        startLatch2.countDown();
        endLatch2.await();
        long pessimisticLockDuration = System.currentTimeMillis() - startMillis;

        // when 3 - 분산락
        CountDownLatch startLatch3 = new CountDownLatch(1);
        CountDownLatch endLatch3 = new CountDownLatch(tryCount);

        for (int i = 0; i < tryCount; i++) {
            executorService.execute(() -> {
                try {
                    startLatch3.await();
                    for (int j = 0; j < repeatCount; j++) {
                        pointFinder.findWithDistributionLock(point.getId());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch3.countDown();
                }
            });
        }

        startMillis = System.currentTimeMillis();
        startLatch3.countDown();
        endLatch3.await();
        long distributionLockDuration = System.currentTimeMillis() - startMillis;

        // then
        assertThat(optimisticLockDuration).isLessThan(pessimisticLockDuration);
        assertThat(optimisticLockDuration).isLessThan(distributionLockDuration);
        assertThat(pessimisticLockDuration).isLessThan(distributionLockDuration);
    }

    private Point createPoint() {
        User user = User.builder()
                .username("testUser")
                .build();
        userJpaRepository.save(user);

        Point point = Point.builder()
                .amount(0)
                .user(user)
                .build();
        return pointJpaRepository.save(point);
    }
}
