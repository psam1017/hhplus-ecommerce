package hhplus.ecommerce.server.integration.interfaces.point;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.application.PointFacade;
import hhplus.ecommerce.server.domain.point.service.PointCommand;
import hhplus.ecommerce.server.interfaces.controller.point.PointController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
public class PointControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PointFacade pointFacade;

    @Autowired
    ObjectMapper objectMapper;

    protected String createJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("포인트 조회 API 테스트")
    void getPointTest() throws Exception {
        // given
        Long userId = 1L;
        Integer point = 50000;
        when(pointFacade.getPoint(userId))
                .thenReturn(point);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/users/{userId}/points", userId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(point));
    }

    @Test
    @DisplayName("포인트 충전 API 테스트")
    void chargePointTest() throws Exception {
        // given
        Long userId = 1L;
        Integer chargeAmount = 10000;
        Integer updatedPoint = 60000;
        PointCommand.ChargePoint pointCreate = new PointCommand.ChargePoint(chargeAmount);

        when(pointFacade.chargePoint(eq(userId), any(PointCommand.ChargePoint.class)))
                .thenReturn(updatedPoint);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/users/{userId}/points/charge", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(pointCreate))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(updatedPoint));
    }
}
