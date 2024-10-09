package hhplus.ecommerce.server.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import hhplus.ecommerce.server.infrastructure.jwt.JwtUtils;
import hhplus.ecommerce.server.infrastructure.p6spy.P6SpyEventListener;
import hhplus.ecommerce.server.infrastructure.p6spy.P6SpyFormatter;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

@EnableJpaAuditing
@Configuration
public class InfrastructureConfig {

    @Bean
    public JwtUtils jwtUtils(@Value("${hhplus.security.token.secret-key}") String secretKey) {
        return new JwtUtils(secretKey);
    }

    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public P6SpyEventListener p6SpyCustomEventListener() {
        return new P6SpyEventListener();
    }

    @Bean
    public P6SpyFormatter p6SpyCustomFormatter() {
        return new P6SpyFormatter();
    }

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof UserDetails userDetails) {

                return Optional.of(Long.parseLong(userDetails.getUsername()));
            }
            return Optional.empty();
        };
    }
}
