package hhplus.ecommerce.server.integration.interfaces.controller.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.application.CartFacade;
import hhplus.ecommerce.server.domain.cart.service.CartCommand;
import hhplus.ecommerce.server.domain.cart.service.CartInfo;
import hhplus.ecommerce.server.interfaces.controller.cart.CartController;
import hhplus.ecommerce.server.interfaces.controller.cart.CartDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    CartFacade cartFacade;

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
    @DisplayName("장바구니에 상품을 담으면 이를 저장할 수 있다.")
    void putItemIntoCartTest() throws Exception {
        // given
        CartInfo.CartDetail cartDetail = new CartInfo.CartDetail(1L, 101L, "사과", 1000, 2);
        when(cartFacade.putItem(any(CartCommand.PutItem.class)))
                .thenReturn(cartDetail);

        CartDto.CartItemPut cartItemPut = new CartDto.CartItemPut(2);

        // when
        ResultActions resultActions = mockMvc.perform(
                put("/api/users/{userId}/carts/{itemId}", 1L, 101L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson(cartItemPut))
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.itemId").value(101L))
                .andExpect(jsonPath("$.name").value("사과"))
                .andExpect(jsonPath("$.price").value(1000))
                .andExpect(jsonPath("$.amount").value(2));
    }

    @Test
    @DisplayName("사용자 ID 가 있으면 장바구니 목록을 조회할 수 있다.")
    void getCartItemsTest() throws Exception {
        // given
        Long userId = 1L;
        List<CartInfo.CartDetail> cartItemResponseList = List.of(
                new CartInfo.CartDetail(1L, 101L, "사과", 1000, 3),
                new CartInfo.CartDetail(2L, 102L, "바나나", 2000, 1)
        );

        when(cartFacade.getCartItems(userId))
                .thenReturn(cartItemResponseList);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/users/{userId}/carts", userId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].itemId").value(101L))
                .andExpect(jsonPath("$.items[0].name").value("사과"))
                .andExpect(jsonPath("$.items[0].price").value(1000))
                .andExpect(jsonPath("$.items[0].amount").value(3))
                .andExpect(jsonPath("$.items[1].id").value(2L))
                .andExpect(jsonPath("$.items[1].itemId").value(102L))
                .andExpect(jsonPath("$.items[1].name").value("바나나"))
                .andExpect(jsonPath("$.items[1].price").value(2000))
                .andExpect(jsonPath("$.items[1].amount").value(1))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("장바구니 ID 를 보내면 장바구니 목록에서 상품을 삭제할 수 있다.")
    void deleteItemFromCartTest() throws Exception {
        // given
        Long userId = 1L;
        Long cartId = 101L;
        when(cartFacade.deleteCartItem(userId, cartId))
                .thenReturn(cartId);

        // when
        ResultActions resultActions = mockMvc.perform(
                delete("/api/users/{userId}/carts/{cartId}", userId, cartId)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartId))
                .andDo(MockMvcResultHandlers.print());
    }
}
