package hhplus.ecommerce.server.integration.interfaces.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class CorsFilterTest {

    @Autowired
    protected MockMvc mockMvc;

    private static Stream<Arguments> provideAllowedOrigins() {
        return Stream.of(
                Arguments.of("https://localhost:3000"),
                Arguments.of("https://localhost:8080")
        );
    }

    @MethodSource("provideAllowedOrigins")
    @ParameterizedTest
    @DisplayName("허용된 Origin 은 비동기 데이터 요청을 수행할 수 있다.")
    void allowedOrigins(String origin) throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(
                get("")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", origin)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isNotFound());
    }

    @DisplayName("허용되지 않은 Origin 은 비동기 데이터 요청을 수행할 수 없다.")
    @Test
    void notAllowedOrigins() throws Exception {
        // given
        String origin = "https://localhost:4000";

        // when
        ResultActions resultActions = mockMvc.perform(
                get("")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", origin)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isForbidden());
    }
}
