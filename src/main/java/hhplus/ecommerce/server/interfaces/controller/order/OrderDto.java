package hhplus.ecommerce.server.interfaces.controller.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class OrderDto {

    public record OrderCreate(
            @NotEmpty @Valid
            @Schema(name = "items", description = "주문 항목 목록", example = "[{\"itemId\":101, \"amount\":2}]")
            List<OrderCreateItem> items
    ) {
    }

    public record OrderCreateItem(
            @NotNull
            @Schema(name = "itemId", description = "주문할 상품의 고유 식별자", example = "101")
            Long itemId,

            @NotNull
            @Schema(name = "amount", description = "주문할 상품의 수량", example = "2")
            Integer amount
    ) {
    }

    @Builder
    public record OrderIdResponse(
            @Schema(name = "id", description = "주문 생성 결과 ID", example = "1001")
            Long id
    ) {
    }
}
