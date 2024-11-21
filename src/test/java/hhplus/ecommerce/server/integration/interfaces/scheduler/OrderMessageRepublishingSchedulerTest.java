package hhplus.ecommerce.server.integration.interfaces.scheduler;

import hhplus.ecommerce.server.interfaces.scheduler.OrderMessageRepublishingScheduler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring 의 @Scheduled 어노테이션을 테스트합니다.
 */
public class OrderMessageRepublishingSchedulerTest {

    @DisplayName("5초마다 카프카로 발행에 실패한 메시지를 재발행할 수 있다.")
    @Test
    void republishOrderMessages() throws NoSuchMethodException {
        // given
        Method republishOrderMessages = OrderMessageRepublishingScheduler.class.getMethod("republishOrderMessages");
        Scheduled scheduled = republishOrderMessages.getAnnotation(Scheduled.class);
        long fixedRate = scheduled.fixedRate();

        // then
        assertThat(fixedRate).isEqualTo(5000);
    }
}
