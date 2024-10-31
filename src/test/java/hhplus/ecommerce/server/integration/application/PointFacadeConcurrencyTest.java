package hhplus.ecommerce.server.integration.application;

import hhplus.ecommerce.server.application.PointFacade;
import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.service.PointCommand;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.point.PointJpaRepository;
import hhplus.ecommerce.server.infrastructure.user.UserJpaRepository;
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

    @AfterEach
    void tearDown() {
        pointJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @DisplayName("동시에 발생한 10번의 포인트 충전 중 한 건 이상만 성공시킬 수 있다.")
    @Test
    void chargePoint() throws InterruptedException {
        // given
        User user = createUser("testUser");
        createPoint(0, user);
        int tryCount = 10;
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

        // then - 컴퓨터 내부 동작, 커넥션 개수에 따라 정확하게 1건만 성공하지 않을 수 있음
        Integer point = pointFacade.getPoint(user.getId());
        int success = successCount.get();
        assertThat(success).isBetween(1, tryCount);
        assertThat(point).isEqualTo(chargeAmount * success);
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