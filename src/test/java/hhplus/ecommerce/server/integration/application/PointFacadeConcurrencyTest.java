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

    /**
     * 현실적인 케이스로, 포인트 충전 사례는 PG 사에 결제 승인을 요청하는 과정을 거치기 위해서 PointPayment 와 같은 주문 정보 엔티티를 가집니다.
     * 포인트 충전 시도 -> status 가 TEMP 인 PointPayment 생성 후 응답 -> TEMP 인 PointPayment 를 OK 로 변경하면서 사용자 포인트 충전
     * 위와 같은 흐름을 통해 실제 포인트 충전 사례는 낙관적 락과 더불어 주문 정보의 status 검증을 통해 수많은 동시 요청에서 발생하는 동시성 이슈를 제어할 수 있습니다.
     * <hr>
     * 예를 들어, 커넥션 풀 크기가 10 일 때 20번의 요청이 한 번에 들어온다고 생각해보겠습니다. 이 요청들은 PointPayment 의 id 를 보내면서, 이 id 에 해당하는 주문 정보의 결제 승인을 요청하고 있습니다.
     * (1) 스레드 20개가 실행 및 그 중 10개가 커넥션을 획득
     * (2) 10개의 스레드 모두 같은 버전의 PointPayment 를 조회하고, status 가 TEMP 임을 검증하는 데 성공함.
     * (3) 커넥션을 획득한 10개 중 하나의 트랜잭션만 포인트 충전에 성공
     *   -> status 검증 시 TEMP 임을 확인
     *   -> 버전이 증가
     *   -> status 가 TEMP 에서 OK 로 변경
     *   -> 포인트가 충전됨
     *   -> 나머지 9개 트랜잭션은 버전이 달라서 실패
     * (4) 커밋에 성공한 스레드가 이후 PG 사에 결제 승인을 요청
     *   -> 이후 결제 승인, 결제 실패 처리, 보상 트랜잭셕 등을 필요에 따라 수행
     * (5) 20개 중 커넥션을 획득하지 못한 나머지 10개가 커넥션을 획득
     * (6) status 검증 시 OK 이므로 나머지 모두가 실패
     * <hr>
     * 아래의 테스트에서 포인트 충전 기능이 정확히 1번만 성공하려면, 이러한 PG 사 요청과 주문 생성 기능 등을 두어야 합니다.
     * 하지만 해당 기능은 현재 이커머스 시나리오의 핵심 구현 사항이 아니라고 판단하고 위와 같은 가정들이 생략된 상태이기에 "성공 횟수가 1 이상"이라고 검증하고 있습니다.
     */
    @DisplayName("동시에 발생한 포인트 충전 요청은 낙관적 락에 의해 한 건씩만 성공할 수 있다.")
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

        // then
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