package hhplus.ecommerce.server.interfaces.web.common.interceptor;

import hhplus.ecommerce.server.interfaces.web.common.clientinfo.ClientInfoHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@RequiredArgsConstructor
public class AccessLogInterceptor implements HandlerInterceptor {

    private final ClientInfoHolder clientInfoHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {
            if (handler instanceof HandlerMethod) {
                String access = clientInfoHolder.getUsername();
                if (!StringUtils.hasText(access)) {
                    access = clientInfoHolder.getRemoteIp();
                }
                log.info("{} -> {} {}", access, request.getMethod(), request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("AccessLogInterceptor error", e);
        }
        return true;
    }
}
