package hhplus.ecommerce.server.integration.application;

import hhplus.ecommerce.server.application.PointFacade;
import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.service.PointCommand;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.repository.point.PointJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.user.UserJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class PointFacadeConcurrencyTest extends TestContainerEnvironment {

    @Autowired
    PointFacade pointFacade;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    PointJpaRepository pointJpaRepository;

    @DisplayName("동시에 발생한 포인트 충전 요청은 낙관적 락에 의해 한 건씩만 성공할 수 있다.")
    @Test
    void chargePoint() throws InterruptedException {
        // given
        User user = createUser("testUser");
        createPoint(0, user);
        int tryCount = 20;
        int chargeAmount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(tryCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(tryCount);

        AtomicInteger successCount = new AtomicInteger(0);

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < tryCount; i++) {
            tasks.add(() -> {
                try {
                    startLatch.await();
                    pointFacade.chargePoint(user.getId(), new PointCommand.ChargePoint(chargeAmount));
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }
        tasks.forEach(executorService::submit);

        // when
        startLatch.countDown();
        endLatch.await();

        // then
        // 컴퓨터 내부 동작, 커넥션풀 크기에 따라 1건 이상이 성공할 수 있음
        Integer point = pointFacade.getPoint(user.getId());
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);
        assertThat(point).isEqualTo(chargeAmount * successCount.get());
    }

    private User createUser(String username) {
        return userJpaRepository.save(User.builder()
                .username(username)
                .build());
    }

    private Point createPoint(int amount, User user) {
        return pointJpaRepository.save(Point.builder()
                .amount(amount)
                .user(user)
                .build());
    }
}