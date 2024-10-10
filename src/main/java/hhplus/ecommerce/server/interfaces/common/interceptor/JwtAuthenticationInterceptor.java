package hhplus.ecommerce.server.interfaces.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.domain.user.repository.UserQueryRepository;
import hhplus.ecommerce.server.interfaces.common.api.ApiStatus;
import hhplus.ecommerce.server.infrastructure.jwt.IllegalTokenException;
import hhplus.ecommerce.server.infrastructure.jwt.JwtHeader;
import hhplus.ecommerce.server.infrastructure.jwt.JwtStatus;
import hhplus.ecommerce.server.infrastructure.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    // (1) Access Token 이 있는데, 유효한 경우 정상 로직을 수행한다.
    // (2) Access Token 이 유효하지 않은데, Refresh Token 이 없으면 인증 에러로 응답한다.
    // (3) Access Token 과 Refresh Token 둘 다 있는데, Access Token 만 유효하지 않고, Request Refresh Token 과 DB Refresh Token 이 같다면 Access Token 과 Refresh Token 을 새로 만들고 응답한다. 이때, DB 의 Refresh Token 도 새로 갱신한다.
    // (4) Access Token 과 Refresh Token 둘 다 있는데, Access Token 은 유효하지 않고, Request Refresh Token 과 DB Refresh Token 가 다르다면 해킹을 의미한다. DB 의 Refresh Token 을 삭제한다.
    // (5) Access Token 과 Refresh Token 둘 다 있는데, 둘 다 유효하지 않으면 사용할 수 없다.

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final UserQueryRepository userQueryRepository;

    @Transactional
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorization = request.getHeader(AUTHORIZATION);

        if(StringUtils.hasText(authorization) && Pattern.matches("^Bearer .*", authorization)) {
            String accessToken = authorization.replaceFirst("^Bearer ", "");
            Optional<JwtStatus> invalidAccessStatus = jwtUtils.hasInvalidStatus(accessToken);
            // (1)
            if (invalidAccessStatus.isPresent()) {
                String xRefreshToken = request.getHeader(JwtHeader.X_REFRESH_TOKEN.value());
                if (StringUtils.hasText(xRefreshToken) && Pattern.matches("^Bearer .*", xRefreshToken)) {
                    String refreshToken = authorization.replaceFirst("^Bearer ", "");
                    Optional<JwtStatus> invalidRefreshStatus = jwtUtils.hasInvalidStatus(refreshToken);
                    // (2)
                    invalidRefreshStatus.ifPresent(status -> {
                        if (status != JwtStatus.EXPIRED) {
                            log.error("An error occurred at JwtAuthenticationInterceptor. authorization = {}, status = {}", authorization, status.name());
                        }
                        throw new IllegalTokenException(status);
                    });

                    String subject = jwtUtils.extractSubject(refreshToken);
                    User user = userQueryRepository.getById(Long.valueOf(subject));
                    if (Objects.equals(refreshToken, user.getRefreshToken())) {
                        // (3)
                        Claims extraClaims = jwtUtils.extractAllClaims(refreshToken);

                        String accessRenewal = jwtUtils.generateDefaultAccessToken(subject, extraClaims);
                        String refreshRenewal = jwtUtils.generateDefaultRefreshToken(subject, extraClaims);

                        user.renewRefreshToken(refreshToken);

                        // 갱신한 토큰은 프론트한테 다시 보내준다.
                        response.setHeader(JwtHeader.X_ACCESS_RENEWAL.value(), accessRenewal);
                        response.setHeader(JwtHeader.X_REFRESH_RENEWAL.value(), refreshRenewal);
                    } else {
                        // (4)
                        user.removeRefreshToken();
                        log.error("Different Token found at JwtAuthenticationInterceptor. refresh = {}", authorization);
                        sendError403(response, ApiResponse.error(ApiStatus.FORBIDDEN, JwtStatus.class, "사용자가 서로 다른 Refresh Token 을 사용했습니다. 토큰 탈취의 가능성이 있습니다. ", null));
                        return false;
                    }
                } else {
                    // (5)
                    JwtStatus status = invalidAccessStatus.get();
                    if (status != JwtStatus.EXPIRED) {
                        log.error("An error occurred at JwtAuthenticationInterceptor. authorization = {}, status = {}", authorization, status.name());
                    }
                    sendError401(response, ApiResponse.error(ApiStatus.UNAUTHORIZED, JwtStatus.class, "Access Token 이 만료되었습니다. Refresh Token 을 추가하여 다시 한 번 보내보세요. ", null));
                    return false;
                }
            }
        }
        return true;
    }

    private void sendError401(HttpServletResponse response, ApiResponse<?> responseBody) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(responseBody));
        writer.flush();
        writer.close();
    }

    private void sendError403(HttpServletResponse response, ApiResponse<?> responseBody) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setStatus(HttpStatus.FORBIDDEN.value());

        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(responseBody));
        writer.flush();
        writer.close();
    }
}
