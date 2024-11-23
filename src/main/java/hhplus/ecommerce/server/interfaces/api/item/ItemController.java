package hhplus.ecommerce.server.interfaces.api.item;

import hhplus.ecommerce.server.application.ItemFacade;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.item.service.ItemInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
        LocalDateTime endDateTime = LocalDate.now().atStartOfDay();
        LocalDateTime startDateTime = endDateTime.minusDays(3);
        return ItemDto.ItemResponseList.from(itemFacade.findTopItems(startDateTime, endDateTime));
    }

    @Operation(
            summary = "전체 상품 조회",
            description = "전체 상품 목록을 조회합니다.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, schema = @Schema(name = "page", description = "페이지 번호", defaultValue = "1")),
                    @Parameter(in = ParameterIn.QUERY, schema = @Schema(name = "size", description = "페이지 크기", defaultValue = "10")),
                    @Parameter(in = ParameterIn.QUERY, schema = @Schema(name = "prop", description = "정렬 기준", defaultValue = "id")),
                    @Parameter(in = ParameterIn.QUERY, schema = @Schema(name = "dir", description = "정렬 방향", defaultValue = "desc")),
                    @Parameter(in = ParameterIn.QUERY, schema = @Schema(name = "keyword", description = "검색어"))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "전체 상품 목록",
                            content = @Content(
                                    schema = @Schema(implementation = ItemDto.ItemPageInfo.class)
                            )
                    )
            }
    )
    @GetMapping("")
    public ItemDto.ItemPageInfo findItems(
            @Parameter(hidden = true) @ModelAttribute ItemDto.ItemSearchCond searchCond
    ) {
        ItemInfo.ItemPageInfo itemPageInfo = itemFacade.pageItems(ItemCommand.ItemSearchCond.of(
                searchCond.page(),
                searchCond.size(),
                searchCond.prop(),
                searchCond.dir(),
                searchCond.keyword()
        ));
        return ItemDto.ItemPageInfo.from(itemPageInfo.itemDetails(), itemPageInfo.totalCount());
    }
}