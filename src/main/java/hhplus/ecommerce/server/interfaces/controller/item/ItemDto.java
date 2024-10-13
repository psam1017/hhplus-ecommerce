package hhplus.ecommerce.server.interfaces.controller.item;

import hhplus.ecommerce.server.domain.item.service.ItemInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
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
}