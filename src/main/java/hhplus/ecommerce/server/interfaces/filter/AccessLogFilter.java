package hhplus.ecommerce.server.interfaces.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class AccessLogFilter implements Filter {

    // Proxy 서버를 통해 들어온 경우, 클라이언트의 IP 주소를 추출하기 위한 헤더 목록
    private static final String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.trace("AccessLogFilter.doFilter");
        if (request instanceof HttpServletRequest req) {
            String clientIp = extractClientIp(req);
            log.trace("{} -> {} {} {}", clientIp, req.getMethod(), req.getRequestURI(), req.getProtocol());
        }
        chain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}
