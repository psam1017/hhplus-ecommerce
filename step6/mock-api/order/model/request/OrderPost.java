package hhplus.ecommerce.server.interfaces.order.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPost {

    @NotEmpty
    @Valid
    private List<OrderPostItem> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderPostItem {

        @NotNull
        private Long itemId;
        @NotNull
        private Integer amount;
    }
}
