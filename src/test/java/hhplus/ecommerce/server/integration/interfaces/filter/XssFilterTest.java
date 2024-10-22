package hhplus.ecommerce.server.integration.interfaces.filter;

import hhplus.ecommerce.server.interfaces.filter.XssFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

public class XssFilterTest {

    private final XssFilter sut = new XssFilter();

    @DisplayName("Cross-site Scripting 공격이 오면 이를 방어할 수 있다.")
    @Test
    void xssFilter() throws Exception {
        // given
        String header = "<script>alert('xss')</script>"; // & lt;& gt;alert& #40;& #39;xss& #39;& #41;& lt;/& gt;
        String param = "<script>alert('xss')</script>"; // & lt;& gt;alert& #40;& #39;xss& #39;& #41;& lt;/& gt;

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("");
        request.setMethod("GET");
        request.addHeader("Header", header);
        request.addParameter("param", param);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        sut.doFilter(request, response, filterChain);

        // then
        HttpServletRequest filteredRequest = (HttpServletRequest) filterChain.getRequest();
        assertThat(filteredRequest).isNotNull();
        String filteredHeader = filteredRequest.getHeader("Header");
        String filteredParam = filteredRequest.getParameter("param");
        String[] filteredParamterValues = filteredRequest.getParameterValues("param");
        assertThat(filteredHeader).isEqualTo("& lt;& gt;alert& #40;& #39;xss& #39;& #41;& lt;/& gt;");
        assertThat(filteredParam).isEqualTo("& lt;& gt;alert& #40;& #39;xss& #39;& #41;& lt;/& gt;");
        assertThat(filteredParamterValues).containsExactly("& lt;& gt;alert& #40;& #39;xss& #39;& #41;& lt;/& gt;");
    }
}
