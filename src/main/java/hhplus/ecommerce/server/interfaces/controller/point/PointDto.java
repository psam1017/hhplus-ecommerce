package hhplus.ecommerce.server.interfaces.controller.point;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PointDto {
    public record PointCreate(
            @NotNull Long chargeAmount
    ) {
    }

    @Builder
    public record PointResponse(
            Integer point
    ) {
    }
}
