package hhplus.ecommerce.server.interfaces.web.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import hhplus.ecommerce.server.interfaces.web.common.model.api.ApiResponse;
import hhplus.ecommerce.server.interfaces.web.common.model.api.ApiStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * @see <a href="https://docs.spring.io/spring-security/reference/servlet/authentication/events.html">Authentication Events</a>
 * @see <a href="https://www.baeldung.com/spring-security-custom-authentication-failure-handler">Spring Security Custom AuthenticationFailureHandler</a>
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFailureHandlerImpl implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        // Bad credentials
        if(exception instanceof BadCredentialsException) {
            sendError(response, ApiResponse.error(ApiStatus.FORBIDDEN, User.class, "인증정보가 일치하지 않습니다.", null));

        // isAccountNonLocked == false
        } else if(exception instanceof LockedException) {
            sendError(response, ApiResponse.error(ApiStatus.FORBIDDEN, User.class, "계정이 잠겨있습니다.", null));

        // isEnabled == false
        } else if(exception instanceof DisabledException) {
            sendError(response, ApiResponse.error(ApiStatus.FORBIDDEN, User.class, "계정을 사용할 수 없습니다.", null));

        // isAccountNonExpired == false
        } else if(exception instanceof AccountExpiredException) {
            sendError(response, ApiResponse.error(ApiStatus.FORBIDDEN, User.class, "계정이 만료되었습니다.", null));

        // isCredentialsNonExpired == false
        } else if(exception instanceof CredentialsExpiredException) {
            sendError(response, ApiResponse.error(ApiStatus.FORBIDDEN, User.class, "인증정보가 만료되었습니다.", null));

        // An userDetailsService implementation cannot locate a User by its username
        } else if (exception instanceof UsernameNotFoundException) {
            log.error("[UsernameNotFoundException] {}", exception.getMessage());
            sendError(response, ApiResponse.error(ApiStatus.FORBIDDEN, User.class, "사용자를 찾을 수 없습니다.", null));

        // An authentication request could not be processed due to a system problem
        } else if (exception instanceof AuthenticationServiceException) {
            log.error("[AuthenticationServiceException] {}", exception.getMessage());
            sendError(response, ApiResponse.error(ApiStatus.FORBIDDEN, User.class, "인가 서비스에 장애가 발생했습니다. 개발자에게 알려주세요.", null));

        // Other exception
        } else {
            log.error("[AuthenticationException] {}", exception.getMessage());
            sendError(response, ApiResponse.error(ApiStatus.FORBIDDEN, User.class, "알 수 없는 예외가 발생했습니다. 개발자에게 알려주세요.", null));
        }
    }

    private void sendError(HttpServletResponse response, ApiResponse<?> responseBody) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        response.setStatus(HttpStatus.FORBIDDEN.value());

        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(responseBody));
        writer.flush();
        writer.close();
    }
}
