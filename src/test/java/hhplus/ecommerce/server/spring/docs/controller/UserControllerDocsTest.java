package hhplus.ecommerce.server.spring.docs.controller;

import hhplus.ecommerce.server.interfaces.user.model.request.UserLoginPost;
import hhplus.ecommerce.server.spring.docs.RestDocsEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerDocsTest extends RestDocsEnvironment {

    @DisplayName("사용자가 로그인할 수 있다.")
    @Test
    void login() throws Exception {
        // given
        UserLoginPost post = UserLoginPost.builder()
                .username("username")
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/open/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(post))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("username").description("사용자명")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.accessToken").type(STRING).description("액세스 토큰"),
                                fieldWithPath("data.refreshToken").type(STRING).description("리프레시 토큰. X-Refresh-Token 헤더로 전송"),
                                fieldWithPath("data.type").type(STRING).description("토큰 타입")
                        )
                ));
    }
}
