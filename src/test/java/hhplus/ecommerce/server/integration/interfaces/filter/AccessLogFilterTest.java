package hhplus.ecommerce.server.integration.interfaces.filter;

import hhplus.ecommerce.server.interfaces.filter.AccessLogFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.http.HttpMethod.GET;

public class AccessLogFilterTest {

    private final AccessLogFilter sut = new AccessLogFilter();

    @DisplayName("요청을 받으면 AccessLogFilter가 실행된다.")
    @Test
    void accessLogFilter() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("");
        request.setMethod(GET.name());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        // then
        assertThatCode(() -> sut.doFilter(request, response, filterChain))
                .doesNotThrowAnyException();
    }
}
