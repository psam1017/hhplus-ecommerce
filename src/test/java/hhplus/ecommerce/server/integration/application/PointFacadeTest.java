package hhplus.ecommerce.server.integration.application;

import hhplus.ecommerce.server.application.PointFacade;
import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.service.PointCommand;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.point.PointJpaRepository;
import hhplus.ecommerce.server.infrastructure.user.UserJpaRepository;
import hhplus.ecommerce.server.integration.TransactionalTestEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class PointFacadeTest extends TransactionalTestEnvironment {

    @Autowired
    PointFacade pointFacade;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    PointJpaRepository pointJpaRepository;

    @DisplayName("포인트를 조회할 수 있다.")
    @Test
    void getPoint() {
        // given
        User user = createUser("testUser");
        Point point = createPoint(100, user);

        // when
        Integer result = pointFacade.getPoint(user.getId());

        // then
        assertThat(result).isEqualTo(point.getAmount());
    }

    @DisplayName("포인트를 충전할 수 있다.")
    @Test
    void chargePoint() {
        // given
        int leftPoint = 100;
        int chargeAmount = 50;

        User user = createUser("testUser");
        createPoint(leftPoint, user);
        PointCommand.ChargePoint post = new PointCommand.ChargePoint(chargeAmount);

        // when
        Integer result = pointFacade.chargePoint(user.getId(), post);

        // then
        assertThat(result).isEqualTo(leftPoint + chargeAmount);
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