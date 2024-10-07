package hhplus.ecommerce.server.spring.docs.api;

import hhplus.ecommerce.server.spring.docs.RestDocsEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserDocsTest extends RestDocsEnvironment {

    // build.gradle 을 설정한다.
    // RestDocs 를 위한 설정 클래스를 만든다. -> RestDocsConfig, RestDocsEnvironment

    // rest docs 문법 사용해서 테스트 통과한다.

    // adoc 파일이 만들어진다. 어디에? build 에. 왜? build.gradle 에서 그렇게 설정했으니까.
    //      -> adoc 이라는 조각 파일만 만들어졌음

    // 이제 이 snippet 조각들을 모아서 문서를 만들어야 함. 어디서? src/docs/asciidoc/**/xxx.adoc 에서
    // xxx.adoc 같은 거 만들면, 나중에 build 했을 때 html 이 만들어진다.

    // 마지막으로 "build" 했을 때 localhost:8080/rest-docs/index.html 같은 식으로 최종 문서가 만들어진다.
    // clean 하는 이유는, 변경사항 이전의 잘못된 문서들 날아가라고.

    @DisplayName("API 문서를 만들 수 있다.")
    @Test
    void doSomething() throws Exception {
        // given
        String id = "1";
        String name = "John Doe";

        // when
        ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/api/open/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("name", name)
        );

        // then
        // 장점? 단점? 이 생기는데, API 문서를 만들려면 이 테스트를 통과해야 합니다.
        // Swagger 는 테스트로부터 독립적이지만, 제품 코드에 침투적이다.
        // RestDocs 는 제품 코드로부터 독립적이지만, 테스트를 통과해야 문서를 만들 수 있다.
        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("id").description("사용자 ID")
                        ),
                        queryParameters(
                                parameterWithName("name").description("사용자 이름").optional()
                        ),
//                        requestFields( // request body 만들 때 사용한다.
//                                fieldWithPath("data").description("사용자 정보"),
//                                fieldWithPath("data.name").description("사용자 이름"),
//                                fieldWithPath("data.age").description("사용자 나이"),
//                                fieldWithPath("data.isUser").description("사용자 여부")
//                        ),
                        responseFields(
                                fieldWithPath("code").type(STRING).description("응답 코드"),
                                fieldWithPath("message").type(STRING).description("응답 메시지"),
                                fieldWithPath("data").type(OBJECT).description("사용자 정보"),
                                fieldWithPath("data.name").type(STRING).description("사용자 이름"),
                                fieldWithPath("data.age").type(NUMBER).description("사용자 나이"),
                                fieldWithPath("data.isUser").type(BOOLEAN).description("사용자 여부")
                        )
                ));
    }

    /*
    {
        "data": {
            "name": "John Doe",
           "age": 30,
            "isUser": true
        }
    }
     */
    /*

    {
        "code": "200",
        "message": "ok",
        "data": {
            "name": "John Doe",
            "age": 30,
            "isUser": true
        }
    }
     */
}
