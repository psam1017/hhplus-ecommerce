package hhplus.ecommerce.server.interfaces.controller.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class OrderDto {

    public record OrderCreate(
            @NotEmpty @Valid List<OrderCreateItem> items
    ) {
    }

    public record OrderCreateItem(
            @NotNull Long itemId,
            @NotNull Integer amount
    ) {
    }

    @Builder
    public record OrderIdResponse(
            Long id
    ) {
    }
}