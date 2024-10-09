package hhplus.ecommerce.server.spring.docs;

import hhplus.ecommerce.server.ApplicationTests;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.global.web.security.filter.JwtAuthenticationFilter;
import hhplus.ecommerce.server.infrastructure.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Map;

import static hhplus.ecommerce.server.infrastructure.jwt.JwtClaim.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

public class RestDocsEnvironment extends ApplicationTests {

    protected static final String USER_ROLE_DESCRIPTION = "link:enums/UserRole.html[사용자 권한,role='popup']";
    protected static final String USER_STATUS_DESCRIPTION = "link:enums/UserStatus.html[사용자 상태,role='popup']";
    protected static final String REGISTRATION_ID_DESCRIPTION = "link:enums/RegistrationId.html[OAuth2 등록 ID,role='popup']";
    protected static final String ORDER_STATUS_DESCRIPTION = "link:enums/OrderStatus.html[주문 상태,role='popup']";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected RestDocumentationResultHandler restDocs;

    @Autowired
    protected JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider provider) {
        // MockMvc 를 사용할 때는 Filter 를 직접 추가해야 한다.
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(provider))
                .addFilters(jwtAuthenticationFilter)
                .alwaysDo(MockMvcResultHandlers.print())
                .alwaysDo(restDocs)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    protected String createBearerToken(User user) {
        Map<String, Object> claims = Map.of(
                USERNAME.toString(), user.getUsername(),
                USER_STATUS.toString(), user.getStatus().toString(),
                USER_ROLE.toString(), user.getRole().toString()
        );
        return "Bearer " + jwtUtils.generateToken(user.getId().toString(), 1000 * 60, claims);
    }
}
