package hhplus.ecommerce.server.integration.domain.point;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.point.exception.NoSuchPointException;
import hhplus.ecommerce.server.domain.point.exception.OutOfPointException;
import hhplus.ecommerce.server.domain.point.service.PointService;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.item.ItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.point.PointJpaRepository;
import hhplus.ecommerce.server.infrastructure.user.UserJpaRepository;
import hhplus.ecommerce.server.integration.SpringBootTestEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PointServiceTest extends SpringBootTestEnvironment {

    @Autowired
    PointService sut;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    PointJpaRepository pointJpaRepository;

    @Autowired
    ItemJpaRepository itemJpaRepository;

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

//    @DisplayName("락을 걸고 포인트를 조회할 수 있다.")
//    @Test
//    void getPointWithLock() {
//        // given
//        User user = createUser("testUser");
//        Point savedPoint = createPoint(100, user);
//
//        // when
//        Point result = sut.getPointByUserIdWithLock(user.getId());
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(savedPoint.getId());
//        assertThat(result.getAmount()).isEqualTo(savedPoint.getAmount());
//        assertThat(result.getUser().getId()).isEqualTo(user.getId());
//    }
//
//    @DisplayName("락을 걸고 포인트를 조회할 때 포인트가 존재하지 않으면 예외가 발생한다.")
//    @Test
//    void throwNoSuchPointExceptionWhenGetPointWithLock() {
//        // given
//        Long nonExistentUserId = 999L;
//
//        // when
//        // then
//        assertThatThrownBy(() -> sut.getPointByUserIdWithLock(nonExistentUserId))
//                .isInstanceOf(NoSuchPointException.class)
//                .hasMessage(new NoSuchPointException().getMessage());
//    }

    /*


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
     */

    @DisplayName("사용자의 포인트를 사용할 수 있다.")
    @Test
    void usePoint() {
        // given
        User user = createUser("testUser");
        createPoint(5000, user);
        Item item1 = createItem("Item1", 1000);
        Item item2 = createItem("Item2", 2000);

        Map<Long, Integer> itemIdStockAmountMap = Map.of(
                item1.getId(), 1,
                item2.getId(), 2
        );

        // when
        sut.usePoint(user.getId(), List.of(item1, item2), itemIdStockAmountMap);

        // then
        Point result = pointJpaRepository.findByUserId(user.getId()).orElseThrow(NoSuchPointException::new);
        assertThat(result.getAmount()).isZero();
    }

    @DisplayName("존재하지 않는 사용자로 포인트를 사용할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchPointExceptionWhenUsePoint() {
        // given
        Long nonExistentUserId = 999L;

        // when
        // then
        assertThatThrownBy(() -> sut.usePoint(nonExistentUserId, List.of(), Map.of()))
                .isInstanceOf(NoSuchPointException.class)
                .hasMessage(new NoSuchPointException().getMessage());
    }

    @DisplayName("사용자는 보유한 포인트 이상으로 포인트를 사용할 수 없다.")
    @Test
    void throwOutOfPointExceptionWhenUsePoint() {
        // given
        int pointAmount = 4999;
        User user = createUser("testUser");
        createPoint(pointAmount, user);
        Item item1 = createItem("Item1", 1000);
        Item item2 = createItem("Item2", 2000);

        Map<Long, Integer> itemIdStockAmountMap = Map.of(
                item1.getId(), 1,
                item2.getId(), 2
        );

        // when
        // then
        assertThatThrownBy(() -> sut.usePoint(user.getId(), List.of(item1, item2), itemIdStockAmountMap))
                .isInstanceOf(OutOfPointException.class)
                .hasMessage(new OutOfPointException(pointAmount).getMessage());
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

    private Item createItem(String name, int price) {
        Item buildItem = Item.builder()
                .name(name)
                .price(price)
                .build();
        return itemJpaRepository.save(buildItem);
    }
}
