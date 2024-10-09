package hhplus.ecommerce.server.interfaces.point.model.requqest;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointChargePost {

    @NotNull
    private Long chargeAmount;
}
