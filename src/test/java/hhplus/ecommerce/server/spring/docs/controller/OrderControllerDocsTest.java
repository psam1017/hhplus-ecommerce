package hhplus.ecommerce.server.spring.docs.controller;

import hhplus.ecommerce.server.interfaces.order.model.request.OrderPost;
import hhplus.ecommerce.server.interfaces.order.model.request.OrderSlicingSearchCond;
import hhplus.ecommerce.server.spring.docs.RestDocsEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerDocsTest extends RestDocsEnvironment {

    @DisplayName("상품들을 구매할 수 있다.")
    @Test
    void doOrder() throws Exception {
        // given
        String accessToken = "xxx.yyy.zzz";

        List<OrderPost.OrderPostItem> items = List.of(
                OrderPost.OrderPostItem.builder()
                        .itemId(1L)
                        .amount(1)
                        .build(),
                OrderPost.OrderPostItem.builder()
                        .itemId(2L)
                        .amount(2)
                        .build()
        );
        OrderPost post = OrderPost.builder()
                .items(items)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/members/orders/new")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(post))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 토큰")
                        ),
                        requestFields(
                                fieldWithPath("items[].itemId").description("상품 ID"),
                                fieldWithPath("items[].amount").description("수량")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.orderId").description("주문 ID")
                        )
                ));
    }

    @DisplayName("상품을 구매하고 결제를 승인할 수 있다.")
    @Test
    void confirmOrder() throws Exception {
        // given
        String accessToken = "xxx.yyy.zzz";
        Long orderId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/members/orders/{orderId}/confirm", orderId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 토큰")
                        ),
                        pathParameters(
                                parameterWithName("orderId").description("주문 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.orderId").description("주문 ID")
                        )
                ));
    }

    @DisplayName("주문 내역을 조회할 수 있다.")
    @Test
    void getOrders() throws Exception {
        // given
        String accessToken = "xxx.yyy.zzz";
        OrderSlicingSearchCond cond = OrderSlicingSearchCond.builder()
                .size(10)
                .lastSequence(130L)
                .searchStartDateTime(LocalDateTime.now().minusDays(1))
                .searchEndDateTime(LocalDateTime.now())
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/members/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .param("size", String.valueOf(cond.getSize()))
                        .param("lastSequence", String.valueOf(cond.getLastSequence()))
                        .param("searchStartDateTime", cond.getSearchStartDateTime().toString())
                        .param("searchEndDateTime", cond.getSearchEndDateTime().toString())
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 토큰")
                        ),
                        queryParameters(
                                parameterWithName("size").description("페이지 크기"),
                                parameterWithName("lastSequence").description("마지막 시퀀스"),
                                parameterWithName("searchStartDateTime").description("검색 시작 일시"),
                                parameterWithName("searchEndDateTime").description("검색 종료 일시")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.orders[].id").description("주문 ID"),
                                fieldWithPath("data.orders[].title").description("주문명"),
                                fieldWithPath("data.orders[].orderDateTime").description("주문 일시"),
                                fieldWithPath("data.orders[].orderStatus").description(ORDER_STATUS_DESCRIPTION),
                                fieldWithPath("data.orders[].orderStatusValue").description(ORDER_STATUS_DESCRIPTION),
                                fieldWithPath("data.slicingInfo.size").description("조회 요청한 개수"),
                                fieldWithPath("data.slicingInfo.lastSequence").description("마지막 시퀀스"),
                                fieldWithPath("data.slicingInfo.hasNext").description("다음 페이지 존재 여부")
                        )
                ));
    }

    @DisplayName("주문 상세 정보를 조회할 수 있다.")
    @Test
    void getOrderDetail() throws Exception {
        // given
        String accessToken = "xxx.yyy.zzz";
        Long orderId = 1L;

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/members/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestHeaders(
                                headerWithName(HttpHeaders.AUTHORIZATION).description("Bearer 토큰")
                        ),
                        pathParameters(
                                parameterWithName("orderId").description("주문 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.id").description("주문 ID"),
                                fieldWithPath("data.title").description("주문명"),
                                fieldWithPath("data.orderDateTime").description("주문 일시"),
                                fieldWithPath("data.orderStatus").description(ORDER_STATUS_DESCRIPTION),
                                fieldWithPath("data.orderStatusValue").description(ORDER_STATUS_DESCRIPTION),
                                fieldWithPath("data.orderItems[].orderItemId").description("주문 상품 ID"),
                                fieldWithPath("data.orderItems[].itemId").description("상품 ID"),
                                fieldWithPath("data.orderItems[].name").description("상품명"),
                                fieldWithPath("data.orderItems[].price").description("가격"),
                                fieldWithPath("data.orderItems[].amount").description("수량")
                        )
                ));
    }
}
