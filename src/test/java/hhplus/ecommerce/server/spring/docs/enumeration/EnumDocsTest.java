package hhplus.ecommerce.server.spring.docs.enumeration;

import hhplus.ecommerce.server.spring.docs.RestDocsEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.restdocs.snippet.Snippet;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EnumDocsTest extends RestDocsEnvironment {

    @DisplayName("문서화할 수 있는 열거형을 정리할 수 있다.")
    @Test
    void findDocumentableInstances() throws Exception {
        // given
        Map<String, Map<String, String>> documentableInstances = DocumentableUtils.findDocumentableInstances();

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/open/enums")
                        .accept(APPLICATION_JSON)
        );

        resultActions.andExpect(status().isOk())
                .andDo(document("enums",
                        documentableInstances.keySet().stream().map((k) ->
                                enumFieldSnippet(
                                        "enum",
                                        attributes(key("title").value(k)),
                                        beneathPath(k).withSubsectionId(k),
                                        enumConvertFieldDescriptor(documentableInstances.get(k))
                                )).toArray(Snippet[]::new)
                ));
    }

    private FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> map) {
        return map.keySet().stream()
                .map(key -> fieldWithPath(key).description(map.get(key)))
                .toArray(FieldDescriptor[]::new);
    }

    public static EnumFieldSnippet enumFieldSnippet(String type,
                                                    Map<String, Object> attributes,
                                                    PayloadSubsectionExtractor<?> subsectionExtractor,
                                                    FieldDescriptor... descriptors) {
        return new EnumFieldSnippet(type, Arrays.asList(descriptors), attributes, true, subsectionExtractor);
    }
}
