package hhplus.ecommerce.server.global.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hhplus.ecommerce.server.domain.user.repository.UserQueryRepository;
import hhplus.ecommerce.server.global.web.argument.RemoteIpArgumentResolver;
import hhplus.ecommerce.server.global.web.argument.TokenArgumentResolver;
import hhplus.ecommerce.server.global.web.argument.UserIdArgumentResolver;
import hhplus.ecommerce.server.global.web.interceptor.AccessLogInterceptor;
import hhplus.ecommerce.server.global.web.interceptor.ClientInfoHolderInterceptor;
import hhplus.ecommerce.server.global.web.interceptor.JwtAuthenticationInterceptor;
import hhplus.ecommerce.server.global.web.validator.EnumPatternValidator;
import hhplus.ecommerce.server.global.web.clientinfo.ClientInfoHolder;
import hhplus.ecommerce.server.infrastructure.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.CacheControl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@EnableScheduling
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Environment env;
    private final JwtUtils jwtUtils;
    private final UserQueryRepository userQueryRepository;

    @Bean
    public EnumPatternValidator enumPatternValidator() {
        return new EnumPatternValidator();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 필요한 경우 프론트엔드와 협의.
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ClientInfoHolder clientInfoHolder() {
        return new ClientInfoHolder();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new UserIdArgumentResolver(jwtUtils));
        resolvers.add(new TokenArgumentResolver(jwtUtils));
        resolvers.add(new RemoteIpArgumentResolver(clientInfoHolder()));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ClientInfoHolderInterceptor(jwtUtils, objectMapper(), clientInfoHolder()))
                .addPathPatterns("/**")
                .order(1);
        registry.addInterceptor(new AccessLogInterceptor(clientInfoHolder()))
                .addPathPatterns("/**")
                .order(2);

        // "test" 프로필이 아닐 때만 Interceptor 를 등록
        if (!Arrays.asList(env.getActiveProfiles()).contains("test")) {
            registry.addInterceptor(new JwtAuthenticationInterceptor(jwtUtils, objectMapper(), userQueryRepository))
                    .addPathPatterns("/api/**")
                    .order(3);
        }
    }

    // src/main/resources/static/images 경로의 이미지 파일을 캐싱
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic())
                .setUseLastModified(true); // default is also true
    }
}
