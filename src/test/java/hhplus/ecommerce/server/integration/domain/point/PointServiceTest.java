package hhplus.ecommerce.server.integration.domain.point;

import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.exception.NoSuchPointException;
import hhplus.ecommerce.server.domain.point.service.PointService;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.point.PointJpaRepository;
import hhplus.ecommerce.server.infrastructure.user.UserJpaRepository;
import hhplus.ecommerce.server.integration.domain.ServiceTestEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PointServiceTest extends ServiceTestEnvironment {

    @Autowired
    PointService sut;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    PointJpaRepository pointJpaRepository;

    @DisplayName("포인트를 조회할 수 있다.")
    @Test
    void getPoint() {
        // given
        User user = createUser("testUser");
        Point savedPoint = createPoint(100, user);

        // when
        Point result = sut.getPointByUserId(user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedPoint.getId());
        assertThat(result.getAmount()).isEqualTo(savedPoint.getAmount());
        assertThat(result.getUser().getId()).isEqualTo(user.getId());
    }

    @DisplayName("포인트가 존재하지 않을 경우 예외가 발생한다.")
    @Test
    void throwNoSuchPointExceptionWhenGetPoint() {
        // given
        Long nonExistentUserId = 999L;

        // when
        // then
        assertThatThrownBy(() -> sut.getPointByUserId(nonExistentUserId))
                .isInstanceOf(NoSuchPointException.class)
                .hasMessage(new NoSuchPointException().getMessage());
    }

    @DisplayName("락을 걸고 포인트를 조회할 수 있다.")
    @Test
    void getPointWithLock() {
        // given
        User user = createUser("testUser");
        Point savedPoint = createPoint(100, user);

        // when
        Point result = sut.getPointByUserIdWithLock(user.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedPoint.getId());
        assertThat(result.getAmount()).isEqualTo(savedPoint.getAmount());
        assertThat(result.getUser().getId()).isEqualTo(user.getId());
    }

    @DisplayName("락을 걸고 포인트를 조회할 때 포인트가 존재하지 않으면 예외가 발생한다.")
    @Test
    void throwNoSuchPointExceptionWhenGetPointWithLock() {
        // given
        Long nonExistentUserId = 999L;

        // when
        // then
        assertThatThrownBy(() -> sut.getPointByUserIdWithLock(nonExistentUserId))
                .isInstanceOf(NoSuchPointException.class)
                .hasMessage(new NoSuchPointException().getMessage());
    }

    @DisplayName("포인트를 충전할 수 있다.")
    @Test
    void chargePoint() {
        // given
        int leftPoint = 100;
        User user = createUser("testUser");
        createPoint(leftPoint, user);
        int chargeAmount = 50;

        // when
        Point result = sut.chargePoint(user.getId(), chargeAmount);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(leftPoint + chargeAmount);
        assertThat(result.getUser().getId()).isEqualTo(user.getId());
    }

    @DisplayName("포인트를 충전할 때 포인트가 존재하지 않으면 예외가 발생한다.")
    @Test
    void throwNoSuchPointExceptionWhenChargePoint() {
        // given
        Long nonExistentUserId = 999L;
        int chargeAmount = 50;

        // when
        // then
        assertThatThrownBy(() -> sut.chargePoint(nonExistentUserId, chargeAmount))
                .isInstanceOf(NoSuchPointException.class)
                .hasMessage(new NoSuchPointException().getMessage());
    }

    private User createUser(String username) {
        User buildUser = User.builder()
                .username(username)
                .build();
        return userJpaRepository.save(buildUser);
    }

    private Point createPoint(int amount, User user) {
        Point buildPoint = Point.builder()
                .amount(amount)
                .user(user)
                .build();
        return pointJpaRepository.save(buildPoint);
    }
}
