package hhplus.ecommerce.server.interfaces.web.support.security.userdetails;

import hhplus.ecommerce.server.domain.user.exception.NoSuchUserException;
import hhplus.ecommerce.server.infrastructure.jwt.JwtClaim;
import hhplus.ecommerce.server.infrastructure.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final JwtUtils jwtUtils;

    // UserDetailsService 시작
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username is a token
        return resolveToken(username);
    }

    private UserDetails resolveToken(String token) {
        try {
            Claims claims = jwtUtils.extractAllClaims(token);
            return PrincipalDetails.builder()
                    .username(claims.getSubject())
                    .status(claims.get(JwtClaim.USER_STATUS.key(), String.class))
                    .role(claims.get(JwtClaim.USER_ROLE.key(), String.class))
                    .build();
        } catch (JwtException | NoSuchUserException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }
    }
    // UserDetailsService 끝
}
