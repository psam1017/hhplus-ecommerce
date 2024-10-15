package hhplus.ecommerce.server.unit.point;

import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.exception.NoSuchPointException;
import hhplus.ecommerce.server.domain.point.service.PointRepository;
import hhplus.ecommerce.server.domain.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class PointServiceUnitTest {

    @InjectMocks
    PointService sut;

    @Mock
    PointRepository pointRepository;

    @DisplayName("포인트를 조회할 수 있다.")
    @Test
    void getPoint() {
        // given
        Long userId = 1L;
        Point point = Point.builder().build();

        when(pointRepository.findByUserId(userId))
                .thenReturn(Optional.of(point));

        // when
        Point result = sut.getPointByUserId(userId);

        // then
        assertThat(result).isEqualTo(point);
        verify(pointRepository, times(1)).findByUserId(userId);
    }

    @DisplayName("포인트가 존재하지 않을 경우 예외가 발생한다.")
    @Test
    void throwNoSuchPointExceptionWhenGetPoint() {
        // given
        Long userId = 1L;

        when(pointRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> sut.getPointByUserId(userId))
                .isInstanceOf(NoSuchPointException.class)
                .hasMessage(new NoSuchPointException().getMessage());
        verify(pointRepository, times(1)).findByUserId(userId);
    }

    @DisplayName("포인트를 충전할 수 있다.")
    @Test
    void chargePoint() {
        // given
        Long userId = 1L;
        Integer amount = 100;
        Point point = Point.builder().build();

        when(pointRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.of(point));

        // when
        Point result = sut.chargePoint(userId, amount);

        // then
        assertThat(result).isEqualTo(point);
        verify(pointRepository, times(1)).findByUserIdWithLock(userId);
    }

    @DisplayName("포인트를 충전할 때 포인트가 존재하지 않으면 예외가 발생한다.")
    @Test
    void throwNoSuchPointExceptionWhenChargePoint() {
        // given
        Long userId = 1L;
        Integer amount = 100;

        when(pointRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> sut.chargePoint(userId, amount))
                .isInstanceOf(NoSuchPointException.class)
                .hasMessage(new NoSuchPointException().getMessage());
        verify(pointRepository, times(1)).findByUserIdWithLock(userId);
    }
}