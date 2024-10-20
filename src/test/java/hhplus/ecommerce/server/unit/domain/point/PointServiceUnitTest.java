package hhplus.ecommerce.server.unit.domain.point;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.exception.NoSuchPointException;
import hhplus.ecommerce.server.domain.point.exception.OutOfPointException;
import hhplus.ecommerce.server.domain.point.service.PointRepository;
import hhplus.ecommerce.server.domain.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
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

    @DisplayName("사용자의 포인트를 사용할 수 있다.")
    @Test
    void usePoint() {
        // given
        Long userId = 1L;
        List<Item> items = List.of(
                Item.builder().id(1L).price(1000).build(),
                Item.builder().id(2L).price(2000).build()
        );
        Map<Long, Integer> itemIdStockAmountMap = Map.of(
                1L, 1,
                2L, 2
        );
        Point point = Point.builder().amount(5000).build();

        when(pointRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.of(point));

        // when
        sut.usePoint(userId, items, itemIdStockAmountMap);

        // then
        assertThat(point.getAmount()).isZero();
        verify(pointRepository, times(1)).findByUserIdWithLock(userId);
    }

    @DisplayName("존재하지 않는 사용자로 포인트를 사용할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchPointExceptionWhenUsePoint() {
        // given
        Long userId = 1L;
        when(pointRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> sut.usePoint(userId, List.of(), Map.of()))
                .isInstanceOf(NoSuchPointException.class)
                .hasMessage(new NoSuchPointException().getMessage());
    }

    @DisplayName("사용자는 보유한 포인트 이상으로 포인트를 사용할 수 없다.")
    @Test
    void throwOutOfPointExceptionWhenUsePoint() {
        // given
        Long userId = 1L;
        int pointAmount = 4999;
        List<Item> items = List.of(
                Item.builder().id(1L).price(1000).build(),
                Item.builder().id(2L).price(2000).build()
        );
        Map<Long, Integer> itemIdStockAmountMap = Map.of(
                1L, 1,
                2L, 2
        );
        Point point = Point.builder().amount(pointAmount).build();

        when(pointRepository.findByUserIdWithLock(userId))
                .thenReturn(Optional.of(point));

        // when
        // then
        assertThatThrownBy(() -> sut.usePoint(userId, items, itemIdStockAmountMap))
                .isInstanceOf(OutOfPointException.class)
                .hasMessage(new OutOfPointException(pointAmount).getMessage());
    }
}
