package hhplus.ecommerce.server.integration.interfaces.interceptor;

import hhplus.ecommerce.server.infrastructure.jwt.JwtUtils;
import hhplus.ecommerce.server.infrastructure.security.UserIdHolder;
import hhplus.ecommerce.server.integration.SpringBootTestEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class UserIdInterceptorTest extends SpringBootTestEnvironment {

    private static MockedStatic<UserIdHolder> mockedStatic;

    @MockBean
    JwtUtils jwtUtils;

    @BeforeAll
    static void beforeAll() {
        mockedStatic = mockStatic(UserIdHolder.class);
    }

    @AfterAll
    static void afterAll() {
        mockedStatic.close();
    }

    @DisplayName("토큰이 전달되면 이를 ThreadLocal 에서 비즈니스 로직이 끝날 때까지 보관할 수 있다.")
    @Test
    void userIdInterceptor() throws Exception {
        // mock
        when(jwtUtils.hasId(anyString())).thenReturn(true);
        when(jwtUtils.getId(anyString())).thenReturn("userId");

        // given
        String authorization = "Bearer token";

        // when
        mockMvc.perform(get("").header(HttpHeaders.AUTHORIZATION, authorization));

        // then
        verify(jwtUtils, times(1)).hasId(anyString());
        verify(jwtUtils, times(1)).getId(anyString());
        mockedStatic.verify(() -> UserIdHolder.setUserId("userId"), times(1));
        mockedStatic.verify(UserIdHolder::clear, times(1));
    }
}
