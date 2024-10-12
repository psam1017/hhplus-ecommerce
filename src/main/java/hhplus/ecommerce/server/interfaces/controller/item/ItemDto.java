package hhplus.ecommerce.server.interfaces.controller.item;

import lombok.Builder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemDto {

    @Builder
    public record ItemResponse(
            Long id,
            String name,
            Integer price,
            Integer amount
    ) {
    }
}
