package hhplus.ecommerce.server.interfaces.web.order.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderPostResult {

    private Long orderId;

    @Builder
    protected OrderPostResult(Long orderId) {
        this.orderId = orderId;
    }
}
