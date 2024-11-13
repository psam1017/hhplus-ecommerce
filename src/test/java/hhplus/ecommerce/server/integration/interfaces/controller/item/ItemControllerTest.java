package hhplus.ecommerce.server.integration.interfaces.controller.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import hhplus.ecommerce.server.application.ItemFacade;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.item.service.ItemInfo;
import hhplus.ecommerce.server.interfaces.controller.item.ItemController;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ItemFacade itemFacade;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인을 하지 않아도 API 를 호출하면 인기 상품 목록을 조회할 수 있다.")
    void findTopItemsTest() throws Exception {
        // given
        List<ItemInfo.ItemDetail> topItems = List.of(
                new ItemInfo.ItemDetail(101L, "사과", 1000, 10),
                new ItemInfo.ItemDetail(102L, "바나나", 2000, 5)
        );

        when(itemFacade.findTopItems(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(topItems);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/items/top")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(101L))
                .andExpect(jsonPath("$.items[0].name").value("사과"))
                .andExpect(jsonPath("$.items[0].price").value(1000))
                .andExpect(jsonPath("$.items[0].amount").value(10))
                .andExpect(jsonPath("$.items[1].id").value(102L))
                .andExpect(jsonPath("$.items[1].name").value("바나나"))
                .andExpect(jsonPath("$.items[1].price").value(2000))
                .andExpect(jsonPath("$.items[1].amount").value(5));
    }

    @Test
    @DisplayName("로그인을 하지 않아도 API 를 호출하면 전체 상품 목록을 조회할 수 있다.")
    void findItemsTest() throws Exception {
        // given
        List<ItemInfo.ItemDetail> allItems = List.of(
                new ItemInfo.ItemDetail(101L, "(과일)사과", 1000, 10),
                new ItemInfo.ItemDetail(102L, "(과일)바나나", 2000, 5),
                new ItemInfo.ItemDetail(103L, "(과일)포도", 3000, 15)
        );
        long totalCount = 3;
        ItemInfo.ItemPageInfo itemPageInfo = ItemInfo.ItemPageInfo.from(allItems, totalCount);

        when(itemFacade.pageItems(any(ItemCommand.ItemSearchCond.class))).thenReturn(itemPageInfo);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "1")
                        .param("size", "10")
                        .param("prop", "id")
                        .param("dir", "desc")
                        .param("keyword", "과일")
        );

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(101L))
                .andExpect(jsonPath("$.items[0].name").value("(과일)사과"))
                .andExpect(jsonPath("$.items[0].price").value(1000))
                .andExpect(jsonPath("$.items[0].amount").value(10))
                .andExpect(jsonPath("$.items[1].id").value(102L))
                .andExpect(jsonPath("$.items[1].name").value("(과일)바나나"))
                .andExpect(jsonPath("$.items[1].price").value(2000))
                .andExpect(jsonPath("$.items[1].amount").value(5))
                .andExpect(jsonPath("$.items[2].id").value(103L))
                .andExpect(jsonPath("$.items[2].name").value("(과일)포도"))
                .andExpect(jsonPath("$.items[2].price").value(3000))
                .andExpect(jsonPath("$.items[2].amount").value(15))
                .andExpect(jsonPath("$.totalCount").value(3));
    }
}
