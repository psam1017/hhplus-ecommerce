package hhplus.ecommerce.server.global.web.security.filter;

import hhplus.ecommerce.server.infrastructure.jwt.JwtHeader;
import hhplus.ecommerce.server.infrastructure.jwt.JwtStatus;
import hhplus.ecommerce.server.infrastructure.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    // Token 의 탈취 가능성은 고려하지 않고, 오직 유효성만 검증한다.
    // Token 의 탈취 가능성을 고려한 대조는 JwtAuthenticationInterceptor 에서 수행한다.
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = null;

            String authorization = request.getHeader(AUTHORIZATION);
            if(StringUtils.hasText(authorization) && Pattern.matches("^Bearer .*", authorization)) {
                String accessToken = authorization.replaceFirst("^Bearer ", "");
                Optional<JwtStatus> accessStatus = jwtUtils.hasInvalidStatus(accessToken);

                if (accessStatus.isEmpty()) {
                    userDetails = userDetailsService.loadUserByUsername(accessToken);
                } else {
                    String xRefreshToken = request.getHeader(JwtHeader.X_REFRESH_TOKEN.value());
                    if (StringUtils.hasText(xRefreshToken) && Pattern.matches("^Bearer .*", xRefreshToken)) {
                        String refreshToken = authorization.replaceFirst("^Bearer ", "");
                        Optional<JwtStatus> refreshStatus = jwtUtils.hasInvalidStatus(refreshToken);
                        if (refreshStatus.isEmpty()) {
                            userDetails = userDetailsService.loadUserByUsername(refreshToken);
                        }
                    }
                }
            }

            if (userDetails != null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
