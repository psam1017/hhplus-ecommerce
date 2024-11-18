package hhplus.ecommerce.server.interfaces.api.point;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

public class PointDto {
    public record PointCreate(
            @Positive
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
