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

    @DisplayName("동시에 발생한 10번의 요청을 충돌 없이 처리할 수 있다.")
    @Test
    void chargePoint() throws InterruptedException {
        // given
        User user = createUser("testUser");
        createPoint(0, user);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(10);

        int totalChargeAmount = 0;
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int chargeAmount = i + 1;
            totalChargeAmount += chargeAmount;
            tasks.add(() -> {
                try {
                    startLatch.await();
                    PointCommand.ChargePoint post = new PointCommand.ChargePoint(chargeAmount);
                    pointFacade.chargePoint(user.getId(), post);
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
        Integer point = pointFacade.getPoint(user.getId());
        assertThat(point).isEqualTo(totalChargeAmount);
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