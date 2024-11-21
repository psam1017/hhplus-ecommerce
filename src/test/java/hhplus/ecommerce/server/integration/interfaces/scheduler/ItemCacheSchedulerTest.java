package hhplus.ecommerce.server.integration.interfaces.scheduler;

import hhplus.ecommerce.server.interfaces.scheduler.ItemCacheScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring 의 @Scheduled 어노테이션을 테스트합니다.
 */
public class ItemCacheSchedulerTest {

    private static Stream<Arguments> provideTimeDividedByTen() {
        return Stream.of(
                Arguments.of(LocalTime.of(0, 0, 0)),
                Arguments.of(LocalTime.of(0, 5, 0)),
                Arguments.of(LocalTime.of(0, 10, 0)),
                Arguments.of(LocalTime.of(0, 15, 0)),
                Arguments.of(LocalTime.of(0, 20, 0)),
                Arguments.of(LocalTime.of(0, 25, 0)),
                Arguments.of(LocalTime.of(0, 30, 0)),
                Arguments.of(LocalTime.of(0, 35, 0)),
                Arguments.of(LocalTime.of(0, 40, 0)),
                Arguments.of(LocalTime.of(0, 45, 0)),
                Arguments.of(LocalTime.of(0, 50, 0)),
                Arguments.of(LocalTime.of(0, 55, 0))
        );
    }

    @MethodSource("provideTimeDividedByTen")
    @DisplayName("상품 페이지네이션 캐시 워밍은 5분마다 실행된다.")
    @ParameterizedTest
    void warmUp(LocalTime time) throws NoSuchMethodException {
        // given
        Method warmUp = ItemCacheScheduler.class.getDeclaredMethod("warmUp");
        Scheduled scheduled = warmUp.getAnnotation(Scheduled.class);
        String cron = scheduled.cron();
        String[] cronSplit = cron.split(" ");
        int cronMinuteDivider = Integer.parseInt(cronSplit[1].split("/")[1]);

        // then
        assertThat(cron).isEqualTo("0 0/5 * * * *");
        assertThat(cronSplit[0]).isEqualTo(String.valueOf(time.getSecond()));
        assertThat(time.getMinute() % cronMinuteDivider).isEqualTo(0);
    }

    @DisplayName("상품 상위 목록 캐시 워밍은 매일 0시 1초에 실행된다.")
    @Test
    void warmUpTopItems() throws NoSuchMethodException {
        // given
        Method warmUpTopItems = ItemCacheScheduler.class.getDeclaredMethod("warmUpTopItems");
        Scheduled scheduled = warmUpTopItems.getAnnotation(Scheduled.class);
        String cron = scheduled.cron();
        String[] cronSplit = cron.split(" ");
        LocalTime time = LocalTime.of(0, 0, 1);

        // then
        assertThat(cron).isEqualTo("1 0 0 * * *");
        assertThat(cronSplit[0]).isEqualTo(String.valueOf(time.getSecond()));
        assertThat(cronSplit[1]).isEqualTo(String.valueOf(time.getMinute()));
        assertThat(cronSplit[2]).isEqualTo(String.valueOf(time.getHour()));
    }
}
