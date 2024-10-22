package hhplus.ecommerce.server.interfaces.interceptor;

import hhplus.ecommerce.server.infrastructure.jwt.JwtUtils;
import hhplus.ecommerce.server.infrastructure.security.UserIdHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Slf4j
public class UserIdInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    // JWT 토큰에서 사용자 ID를 추출하여 UserIdHolder 에 저장
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.trace("UserIdInterceptor.preHandle");
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            if (jwtUtils.hasId(token)) {
                String userId = jwtUtils.getId(token);
                UserIdHolder.setUserId(userId);
            }
        }
        return true;
    }

    // 요청 처리가 완료된 후 (예외가 발생하더라도 항상) UserIdHolder 에 저장된 사용자 ID를 제거
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.trace("UserIdInterceptor.afterCompletion");
        UserIdHolder.clear();
    }
}
