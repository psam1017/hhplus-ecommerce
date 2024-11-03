package hhplus.ecommerce.server.interfaces.controller.item;

import hhplus.ecommerce.server.domain.item.service.ItemInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class ItemDto {

    @Builder
    public record ItemResponse(
            @Schema(name = "id", description = "상품의 고유 식별자", example = "101")
            Long id,

            @Schema(name = "name", description = "상품의 이름", example = "사과")
            String name,

            @Schema(name = "price", description = "상품의 가격", example = "1000")
            Integer price,

            @Schema(name = "amount", description = "상품의 수량", example = "3")
            Integer amount
    ) {
        public static ItemResponse from(ItemInfo.ItemDetail item) {
            return new ItemResponse(
                    item.id(),
                    item.name(),
                    item.price(),
                    item.amount()
            );
        }
    }

    public record ItemResponseList(
            @Schema(name = "items", description = "상품 목록", example = "[{\"id\":101, \"name\":\"사과\", \"price\":1000, \"amount\":3}]")
            List<ItemResponse> items
    ) {
        public static ItemResponseList from(List<ItemInfo.ItemDetail> items) {
            return new ItemResponseList(
                    items.stream()
                            .map(ItemResponse::from)
                            .toList()
            );
        }
    }

    public record ItemSearchCond(
            @Schema(name = "page", description = "페이지 번호", example = "1")
            Integer page,
            @Schema(name = "size", description = "페이지 크기", example = "10")
            Integer size,
            @Schema(name = "prop", description = "정렬 기준", example = "name")
            String prop,
            @Schema(name = "dir", description = "정렬 방향", example = "asc")
            String dir,
            @Schema(name = "keyword", description = "검색어", example = "사과")
            String keyword
    ) {
    }

    public record ItemPageInfo(
            @Schema(name = "items", description = "상품 목록", example = "[{\"id\":101, \"name\":\"사과\", \"price\":1000, \"amount\":3}]")
            List<ItemResponse> items,
            @Schema(name = "totalCount", description = "전체 상품 수", example = "100")
            long totalCount
    ) {
        public static ItemPageInfo from(List<ItemInfo.ItemDetail> itemDetails, long totalCount) {
            return new ItemPageInfo(
                    itemDetails.stream()
                            .map(ItemResponse::from)
                            .toList(),
                    totalCount
            );
        }
    }
}