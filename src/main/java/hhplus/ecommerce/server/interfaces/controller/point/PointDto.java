package hhplus.ecommerce.server.interfaces.controller.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PointDto {
    public record PointCreate(
            @NotNull
            @Schema(name = "chargeAmount", description = "충전할 포인트 금액", example = "10000")
            Integer chargeAmount
    ) {
    }

    @Builder
    public record PointResponse(
            @Schema(name = "point", description = "현재 포인트 잔액", example = "50000")
            Integer point
    ) {
    }
}
