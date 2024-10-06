package hhplus.ecommerce.server.spring.docs.exception;

import hhplus.ecommerce.server.spring.docs.RestDocsEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unchecked")
public class ApiExceptionDocsTest extends RestDocsEnvironment {

    @DisplayName("문서화할 수 있는 예외를 정리할 수 있다.")
    @Test
    void findApiExceptionInstances() throws Exception {
        // given
        Map<String, Map<String, String>> apiExceptionInstancesWrapper = new HashMap<>();

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/open/api-ex")
        );

        resultActions.andDo(r -> {
            String contentAsString = r.getResponse().getContentAsString();
            System.out.println("contentAsString = " + contentAsString);
            apiExceptionInstancesWrapper.putAll(objectMapper.readValue(contentAsString, Map.class));
        });

        resultActions.andExpect(status().isOk())
                .andDo(document("exception",
                        apiExceptionInstancesWrapper.keySet().stream().map((k) ->
                                enumFieldSnippet(
                                        "exception",
                                        attributes(key("title").value(k)),
                                        beneathPath(k).withSubsectionId(k),
                                        enumConvertFieldDescriptor(apiExceptionInstancesWrapper.get(k))
                                )).toArray(Snippet[]::new)
                ));
    }

    private FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> map) {
        return map.keySet().stream()
                .map(key -> fieldWithPath(key).description(map.get(key)))
                .toArray(FieldDescriptor[]::new);
    }

    public static ExceptionFieldSnippet enumFieldSnippet(String type,
                                                         Map<String, Object> attributes,
                                                         PayloadSubsectionExtractor<?> subsectionExtractor,
                                                         FieldDescriptor... descriptors) {
        return new ExceptionFieldSnippet(type, Arrays.asList(descriptors), attributes, true, subsectionExtractor);
    }
}
