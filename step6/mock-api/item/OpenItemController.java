package hhplus.ecommerce.server.interfaces.item;

import hhplus.ecommerce.server.application.common.pagination.PageInfo;
import hhplus.ecommerce.server.interfaces.item.model.request.ItemPageSearchCond;
import hhplus.ecommerce.server.interfaces.item.model.response.ItemDetail;
import hhplus.ecommerce.server.interfaces.item.model.response.ItemPageResult;
import hhplus.ecommerce.server.interfaces.item.model.response.ItemSummary;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/open/items")
@RestController
public class OpenItemController {

    @GetMapping("/top")
    public ApiResponse<List<ItemSummary>> findTopItems() {
        return ApiResponse.ok(buildItemResponses());
    }

    @Operation(
            parameters = {
                    @Parameter(name = "page"),
                    @Parameter(name = "size"),
                    @Parameter(name = "prop"),
                    @Parameter(name = "dir"),
                    @Parameter(name = "search"),
                    @Parameter(name = "atLeastPrice"),
                    @Parameter(name = "atMostPrice"),
            }
    )
    @GetMapping("")
    public ApiResponse<ItemPageResult> findItems(
            @Parameter(hidden = true) @ModelAttribute @Valid ItemPageSearchCond cond
    ) {
        List<ItemSummary> items = buildItemResponses();
        ItemPageResult result = ItemPageResult.builder()
                .items(items)
                .pageInfo(new PageInfo(10, 10, 100, 5))
                .build();
        return ApiResponse.ok(result);
    }

    @GetMapping("/{itemId}")
    public ApiResponse<ItemDetail> getItem(
            @PathVariable Long itemId
    ) {
        ItemDetail itemDetail = ItemDetail.builder()
                .id(itemId)
                .name("상품1")
                .price(1000)
                .amount(10)
                .description("상품1의 설명")
                .build();
        return ApiResponse.ok(itemDetail);
    }

    private static List<ItemSummary> buildItemResponses() {
        return List.of(
                ItemSummary.builder()
                        .id(1L)
                        .name("상품1")
                        .price(1000)
                        .amount(10)
                        .build(),
                ItemSummary.builder()
                        .id(2L)
                        .name("상품2")
                        .price(2000)
                        .amount(20)
                        .build(),
                ItemSummary.builder()
                        .id(3L)
                        .name("상품3")
                        .price(3000)
                        .amount(30)
                        .build(),
                ItemSummary.builder()
                        .id(4L)
                        .name("상품4")
                        .price(4000)
                        .amount(40)
                        .build(),
                ItemSummary.builder()
                        .id(5L)
                        .name("상품5")
                        .price(5000)
                        .amount(50)
                        .build()
        );
    }


}
