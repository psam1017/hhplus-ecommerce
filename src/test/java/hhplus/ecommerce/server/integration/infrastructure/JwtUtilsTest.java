package hhplus.ecommerce.server.integration.infrastructure;

import hhplus.ecommerce.server.infrastructure.jwt.JwtUtils;
import hhplus.ecommerce.server.integration.SpringBootTestEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtUtilsTest extends SpringBootTestEnvironment {

    @Autowired
    JwtUtils jwtUtils;

    @DisplayName("토큰에 아이디가 있는지 검증할 수 있다.")
    @Test
    void hasId() {
        // given
        String token = createToken();

        // when
        boolean hasId = jwtUtils.hasId(token);

        // then
        assertThat(hasId).isTrue();
    }

    @DisplayName("토큰에서 아이디를 추출할 수 있다.")
    @Test
    void getId() {
        // given
        String token = createToken();

        // when
        String id = jwtUtils.getId(token);

        // then
        assertThat(id).isNotBlank();
    }

    private static String createToken() {
        return "Bearer token";
    }
}
