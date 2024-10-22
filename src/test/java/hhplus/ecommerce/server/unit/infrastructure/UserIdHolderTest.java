package hhplus.ecommerce.server.unit.infrastructure;

import hhplus.ecommerce.server.infrastructure.security.UserIdHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserIdHolderTest {

    @DisplayName("UserIdHolder 는 사용자의 id 를 보관하고 반환할 수 있다.")
    @Test
    void setAndGet() {
        // given
        String id = "id";

        // when
        UserIdHolder.setUserId(id);
        String userId = UserIdHolder.getUserId();

        // then
        assertThat(userId).isEqualTo(id);
    }

    @DisplayName("UserIdHolder 에서 사용자의 id 를 제거할 수 있다.")
    @Test
    void setAndClear() {
        // given
        String id = "id";
        UserIdHolder.setUserId(id);

        // when
        UserIdHolder.clear();

        // then
        assertThat(UserIdHolder.getUserId()).isNull();
    }
}
