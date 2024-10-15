package hhplus.ecommerce.server.unit.point;

import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.exception.OutOfPointException;
import hhplus.ecommerce.server.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PointTest {
    
    @DisplayName("포인트를 충전할 수 있다.")
    @Test
    void charge() {
        // given
        int leftAmount = 0;
        int chargeAmount = 1000;

        Point point = Point.builder()
                .amount(leftAmount)
                .user(buildUser())
                .build();
        
        // when
        point.charge(chargeAmount);
        
        // then
        assertThat(point.getAmount()).isEqualTo(chargeAmount);
    }

    @DisplayName("포인트를 사용할 수 있다.")
    @Test
    void usePoint() {
        // given
        int leftAmount = 1000;
        int useAmount = 500;

        Point point = Point.builder()
                .amount(leftAmount)
                .user(buildUser())
                .build();
        
        // when
        point.usePoint(useAmount);
        
        // then
        assertThat(point.getAmount()).isEqualTo(useAmount);
    }

    @DisplayName("보유한 포인트 이상으로 사용할 수 없다.")
    @Test
    void throwOutOfPointException() {
        // given
        int leftAmount = 1000;
        int useAmount = 1500;
        
        Point point = Point.builder()
                .amount(leftAmount)
                .user(buildUser())
                .build();

        // when
        // then
        assertThatThrownBy(() -> point.usePoint(useAmount))
                .isInstanceOf(OutOfPointException.class)
                .hasMessage(new OutOfPointException(leftAmount).getMessage());
    }

    private static User buildUser() {
        return User.builder()
                .build();
    }
}
