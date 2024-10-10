package hhplus.ecommerce.server.interfaces.point.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointDetail {

    private Integer amount;

    @Builder
    protected PointDetail(Integer amount) {
        this.amount = amount;
    }
}
