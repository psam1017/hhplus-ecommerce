package hhplus.ecommerce.server.spring.docs.controller;

import hhplus.ecommerce.server.interfaces.point.model.requqest.PointChargePost;
import hhplus.ecommerce.server.interfaces.point.model.requqest.PointHistorySlicingSearchCond;
import hhplus.ecommerce.server.spring.docs.RestDocsEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PointControllerDocsTest extends RestDocsEnvironment {

    @DisplayName("사용자가 포인트 잔액을 조회할 수 있다.")
    @Test
    void getPointBalance() throws Exception {
        // given
        String accessToken = "xxx.yyy.zzz";

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/members/points")
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
                        relaxedResponseFields(
                                fieldWithPath("data.amount").description("포인트 잔액")
                        )
                ));
    }

    @DisplayName("사용자가 포인트를 충전할 수 있다.")
    @Test
    void chargePoint() throws Exception {
        // given
        String accessToken = "xxx.yyy.zzz";
        PointChargePost post = PointChargePost.builder()
                .chargeAmount(1000L)
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/members/points/charge")
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
                                fieldWithPath("chargeAmount").description("충전할 포인트 금액")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.amount").description("포인트 잔액")
                        )
                ));
    }

    @DisplayName("포인트 충전 내역을 조회할 수 있다.")
    @Test
    void getPointHistories() throws Exception {
        // given
        String accessToken = "xxx.yyy.zzz";
        PointHistorySlicingSearchCond cond = PointHistorySlicingSearchCond.builder()
                .size(10)
                .lastSequence(5L)
                .searchStartDateTime(LocalDateTime.now().minusDays(1))
                .searchEndDateTime(LocalDateTime.now())
                .build();

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/members/points/histories")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
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
                                parameterWithName("size").description("조회할 개수"),
                                parameterWithName("lastSequence").description("마지막 시퀀스"),
                                parameterWithName("searchStartDateTime").description("조회 시작 일시"),
                                parameterWithName("searchEndDateTime").description("조회 종료 일시")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("data.pointHistories[].originalBalance").description("변경 전 포인트 잔액"),
                                fieldWithPath("data.pointHistories[].changeAmount").description("변경 포인트 금액"),
                                fieldWithPath("data.pointHistories[].reason").description("변경 사유"),
                                fieldWithPath("data.pointHistories[].createdDateTime").description("변경 일시"),
                                fieldWithPath("data.slicingInfo.size").description("조회 요청한 개수"),
                                fieldWithPath("data.slicingInfo.lastSequence").description("마지막 시퀀스"),
                                fieldWithPath("data.slicingInfo.hasNext").description("다음 페이지 존재 여부")
                        )
                ));
    }
}
