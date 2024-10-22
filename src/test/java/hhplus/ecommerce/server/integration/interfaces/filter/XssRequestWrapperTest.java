package hhplus.ecommerce.server.integration.interfaces.filter;

import hhplus.ecommerce.server.interfaces.filter.XssRequestWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class XssRequestWrapperTest {

    private MockHttpServletRequest request;
    private XssRequestWrapper sut;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        sut = new XssRequestWrapper(request);
    }

    @DisplayName("Header 의 Cross-site Scripting 공격이 오면 이를 방어할 수 있다.")
    @Test
    void clearXssOnHeader() {
        // given
        String target = "<script>alert('xss')</script>"; // & lt;& gt;alert& #40;& #39;xss& #39;& #41;& lt;/& gt;
        request.setRequestURI("");
        request.setMethod("GET");
        request.addHeader("Header", target);

        // when
        String authorization = sut.getHeader("Header");

        // then
        assertThat(authorization).isEqualTo("& lt;& gt;alert& #40;& #39;xss& #39;& #41;& lt;/& gt;");
    }

    @DisplayName("Parameter 의 Cross-site Scripting 공격이 오면 이를 방어할 수 있다.")
    @Test
    void clearXssOnParameter() {
        // given
        String target = "<script>alert('xss')</script>"; // & lt;& gt;alert& #40;& #39;xss& #39;& #41;& lt;/& gt;

        request.setRequestURI("");
        request.setMethod("GET");
        request.addParameter("param", target);

        // when
        String parameter = sut.getParameter("param");

        // then
        assertThat(parameter).isEqualTo("& lt;& gt;alert& #40;& #39;xss& #39;& #41;& lt;/& gt;");
    }

    @DisplayName("여러 파라미터의 Cross-site Scripting 공격이 오면 이를 방어할 수 있다.")
    @Test
    void cleanXssOnParameterValues() {
        // given
        String target1 = "<script>alert('hello')</script>"; // & lt;& gt;alert& #40;& #39;hello& #39;& #41;& lt;/& gt;
        String target2 = "<script>alert('world')</script>"; // & lt;& gt;alert& #40;& #39;world& #39;& #41;& lt;/& gt;

        request.setRequestURI("");
        request.setMethod("GET");
        request.addParameter("param", target1);
        request.addParameter("param", target2);

        // when
        String[] parameterValues = sut.getParameterValues("param");

        // then
        assertThat(parameterValues).containsExactly(
                "& lt;& gt;alert& #40;& #39;hello& #39;& #41;& lt;/& gt;",
                "& lt;& gt;alert& #40;& #39;world& #39;& #41;& lt;/& gt;"
        );
    }
}
