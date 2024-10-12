package hhplus.ecommerce.server.interfaces.controller.item;

import hhplus.ecommerce.server.application.ItemFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "상품",
        description = "상품에 대한 API"
)
@RequiredArgsConstructor
@RequestMapping("/api/items")
@RestController
public class ItemController {

    private final ItemFacade itemFacade;

    @Operation(
            summary = "상위 상품 조회",
            description = "상위 상품 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상위 상품 목록",
                            content = @Content(
                                    schema = @Schema(implementation = ItemDto.ItemResponseList.class)
                            )
                    )
            }
    )
    @GetMapping("/top")
    public ItemDto.ItemResponseList findTopItems() {
        return ItemDto.ItemResponseList.from(itemFacade.findTopItems());
    }

    @Operation(
            summary = "전체 상품 조회",
            description = "전체 상품 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "전체 상품 목록",
                            content = @Content(
                                    schema = @Schema(implementation = ItemDto.ItemResponseList.class)
                            )
                    )
            }
    )
    @GetMapping("")
    public ItemDto.ItemResponseList findItems() {
        return ItemDto.ItemResponseList.from(itemFacade.findItems());
    }
}