package hhplus.ecommerce.server.interfaces.web.order.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderIdResponse {

    private Long orderId;

    @Builder
    protected OrderIdResponse(Long orderId) {
        this.orderId = orderId;
    }
}
