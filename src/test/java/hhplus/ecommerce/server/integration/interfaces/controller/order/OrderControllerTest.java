package hhplus.ecommerce.server.integration.interfaces.controller.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.application.OrderFacade;
import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.domain.order.service.OrderCommand;
import hhplus.ecommerce.server.domain.order.service.OrderInfo;
import hhplus.ecommerce.server.interfaces.api.order.OrderController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrderFacade orderFacade;

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
    @DisplayName("사용자가 상품 ID 와 수량을 전달하면 해당 상품을 결제할 수 있다.")
    void createOrderTest() throws Exception {
        // given
        Long userId = 1L;
        OrderCommand.CreateOrder orderCreate = new OrderCommand.CreateOrder(
                userId,
                List.of(new OrderCommand.CreateOrderItem(101L, 2))
        );
        Long orderId = 1001L;

        when(orderFacade.createOrder(any(OrderCommand.CreateOrder.class))).thenReturn(orderId);

        // when
        ResultActions resultActions = mockMvc.perform(
                post("/api/users/{userId}/orders", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(orderCreate))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    @DisplayName("사용자 ID 가 있으면 주문 목록을 조회할 수 있다.")
    void getOrdersTest() throws Exception {
        // given
        Long userId = 1L;
        List<OrderInfo.OrderDetail> orders = List.of(
                new OrderInfo.OrderDetail(1001L, LocalDateTime.now(), OrderStatus.ORDERED, 5000),
                new OrderInfo.OrderDetail(1002L, LocalDateTime.now(), OrderStatus.ORDERED, 8000)
        );

        when(orderFacade.findOrders(userId)).thenReturn(orders);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/users/{userId}/orders", userId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders[0].id").value(1001L))
                .andExpect(jsonPath("$.orders[0].orderDateTime").exists())
                .andExpect(jsonPath("$.orders[0].orderStatus").value(OrderStatus.ORDERED.key()))
                .andExpect(jsonPath("$.orders[0].orderAmount").value(5000))
                .andExpect(jsonPath("$.orders[1].id").value(1002L))
                .andExpect(jsonPath("$.orders[1].orderDateTime").exists())
                .andExpect(jsonPath("$.orders[1].orderStatus").value(OrderStatus.ORDERED.key()))
                .andExpect(jsonPath("$.orders[1].orderAmount").value(8000));
    }

    @Test
    @DisplayName("사용자 ID 와 주문 ID 가 있으면 주문 상세를 조회할 수 있다.")
    void getOrderTest() throws Exception {
        // given
        Long userId = 1L;
        Long orderId = 1001L;
        OrderInfo.OrderAndItemDetails orderDetails = new OrderInfo.OrderAndItemDetails(
                new OrderInfo.OrderDetail(orderId, LocalDateTime.now(), OrderStatus.ORDERED, 4000),
                List.of(new OrderInfo.OrderItemDetail(101L, "상품1", 2000, 2))
        );

        when(orderFacade.getOrder(userId, orderId)).thenReturn(orderDetails);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/users/{userId}/orders/{orderId}", userId, orderId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(orderId))
                .andExpect(jsonPath("$.order.orderDateTime").exists())
                .andExpect(jsonPath("$.order.orderStatus").value(OrderStatus.ORDERED.key()))
                .andExpect(jsonPath("$.order.orderAmount").value(4000))
                .andExpect(jsonPath("$.items[0].id").value(101L))
                .andExpect(jsonPath("$.items[0].name").value("상품1"))
                .andExpect(jsonPath("$.items[0].price").value(2000))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }
}
