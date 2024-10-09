package hhplus.ecommerce.server.spring.docs.controller;

import hhplus.ecommerce.server.interfaces.web.item.model.request.ItemPageSearchCond;
import hhplus.ecommerce.server.spring.docs.RestDocsEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ItemControllerDocsTest extends RestDocsEnvironment {

    @DisplayName("매출 상위의 상품들을 조회할 수 있다.")
    @Test
    void findTopItems() throws Exception {
        // given

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/open/items/top")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        relaxedResponseFields(
                                fieldWithPath("data[].id").type(NUMBER).description("상품 ID"),
                                fieldWithPath("data[].name").type(STRING).description("상품명"),
                                fieldWithPath("data[].price").type(NUMBER).description("가격"),
                                fieldWithPath("data[].amount").type(NUMBER).description("수량")
                        )
                ));
    }

    @DisplayName("상품들을 조회할 수 있다.")
    @Test
    void findItems() throws Exception {
        // given
        ItemPageSearchCond cond = ItemPageSearchCond.builder()
                .page(1)
                .size(10)
                .prop("id")
                .dir("asc")
                .search("상품")
                .atLeastPrice(1000)
                .atMostPrice(5000)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/open/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", String.valueOf(cond.getPage()))
                        .param("size", String.valueOf(cond.getSize()))
                        .param("prop", cond.getProp())
                        .param("dir", cond.getDir())
                        .param("search", cond.getSearch())
                        .param("atLeastPrice", String.valueOf(cond.getAtLeastPrice()))
                        .param("atMostPrice", String.valueOf(cond.getAtMostPrice()))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 크기"),
                                parameterWithName("prop").description("정렬 기준"),
                                parameterWithName("dir").description("정렬 방향"),
                                parameterWithName("search").description("검색어"),
                                parameterWithName("atLeastPrice").description("최소 가격"),
                                parameterWithName("atMostPrice").description("최대 가격")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.items[].id").type(NUMBER).description("상품 ID"),
                                fieldWithPath("data.items[].name").type(STRING).description("상품명"),
                                fieldWithPath("data.items[].price").type(NUMBER).description("가격"),
                                fieldWithPath("data.items[].amount").type(NUMBER).description("수량"),
                                fieldWithPath("data.pageInfo.page").type(NUMBER).description("페이지 번호"),
                                fieldWithPath("data.pageInfo.size").type(NUMBER).description("페이지 크기"),
                                fieldWithPath("data.pageInfo.total").type(NUMBER).description("전체 개수"),
                                fieldWithPath("data.pageInfo.lastPage").type(NUMBER).description("마지막 페이지"),
                                fieldWithPath("data.pageInfo.start").type(NUMBER).description("현재 페이지 세트의 첫 페이지 수"),
                                fieldWithPath("data.pageInfo.end").type(NUMBER).description("현재 페이지 세트의 마지막 페이지 수"),
                                fieldWithPath("data.pageInfo.hasPrev").type(BOOLEAN).description("이전 페이지 존재 여부"),
                                fieldWithPath("data.pageInfo.hasNext").type(BOOLEAN).description("다음 페이지 존재 여부")
                        )
                ));
    }

    @DisplayName("상품 상세를 조회할 수 있다.")
    @Test
    void getItem() throws Exception {
        // given
        Long itemId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/open/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("itemId").description("상품 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.id").type(NUMBER).description("상품 ID"),
                                fieldWithPath("data.name").type(STRING).description("상품명"),
                                fieldWithPath("data.price").type(NUMBER).description("가격"),
                                fieldWithPath("data.amount").type(NUMBER).description("수량"),
                                fieldWithPath("data.description").type(STRING).description("설명")
                        )
                ));
    }
}
