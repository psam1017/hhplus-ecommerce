package hhplus.ecommerce.server.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String API_INFO_TITLE = "Swagger Docs";
    private static final String SERVER_URL = "/";
    private static final String SECURITY_SCHEME_NAME = "JWT Bearer token";
    private static final String FORMAT = "JWT";
    private static final String AUTH_TYPE = "bearer";

    @Bean
    public OpenAPI openAPI() {

        Info info = new Info()
                .title(API_INFO_TITLE);
        Server server = new Server()
                .url(SERVER_URL);
        Components components = new Components()
                .addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme(AUTH_TYPE)
                                .bearerFormat(FORMAT)
                );
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME);

        return new OpenAPI()
                .info(info)
                .addServersItem(server)
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}
