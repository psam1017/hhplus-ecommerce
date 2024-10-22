package hhplus.ecommerce.server.config;

import hhplus.ecommerce.server.infrastructure.jwt.JwtUtils;
import hhplus.ecommerce.server.infrastructure.jwt.MockJwtUtils;
import hhplus.ecommerce.server.interfaces.filter.AccessLogFilter;
import hhplus.ecommerce.server.interfaces.filter.XssFilter;
import hhplus.ecommerce.server.interfaces.interceptor.UserIdInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public JwtUtils jwtUtils() {
        return new MockJwtUtils();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserIdInterceptor(jwtUtils()))
                .addPathPatterns("/**")
                .order(1);
    }

    @Bean
    public FilterRegistrationBean<AccessLogFilter> acceeLogFilter() {
        FilterRegistrationBean<AccessLogFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AccessLogFilter());
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        CorsFilter corsFilter = configCorsFilter();
        registrationBean.setFilter(corsFilter);
        registrationBean.setOrder(2);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<XssFilter> xssFilter() {
        FilterRegistrationBean<XssFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new XssFilter());
        registrationBean.setOrder(3);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    private CorsFilter configCorsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> allowedOrigins = List.of(
                "https://localhost:3000",
                "https://localhost:8080"
        );

        configuration.setAllowCredentials(false);
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setMaxAge(6000L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return new CorsFilter(source);
    }
}
