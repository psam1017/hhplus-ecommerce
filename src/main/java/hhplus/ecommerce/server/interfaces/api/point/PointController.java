package hhplus.ecommerce.server.interfaces.api.point;

import hhplus.ecommerce.server.application.PointFacade;
import hhplus.ecommerce.server.domain.point.service.PointCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "포인트",
        description = "사용자의 포인트에 대한 API"
)
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/points")
@RestController
public class PointController {

    private final PointFacade pointFacade;

    @Operation(
            summary = "포인트 조회",
            description = "특정 사용자의 포인트를 조회합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자의 고유 식별자", required = true, in = ParameterIn.PATH)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "사용자의 포인트",
                            content = @Content(
                                    schema = @Schema(implementation = PointDto.PointResponse.class)
                            )
                    )
            }
    )
    @GetMapping("")
    public PointDto.PointResponse getPoint(
            @PathVariable Long userId
    ) {
        return PointDto.PointResponse.builder()
                .point(pointFacade.getPoint(userId))
                .build();
    }

    @Operation(
            summary = "포인트 충전",
            description = "특정 사용자의 포인트를 충전합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "사용자의 고유 식별자", required = true, in = ParameterIn.PATH)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "충전할 포인트 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = PointDto.PointCreate.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "충전된 포인트 정보",
                            content = @Content(
                                    schema = @Schema(implementation = PointDto.PointResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/charge")
    public PointDto.PointResponse chargePoint(
            @PathVariable Long userId,
            @RequestBody @Valid PointDto.PointCreate post
    ) {
        return PointDto.PointResponse.builder()
                .point(pointFacade.chargePoint(userId, new PointCommand.ChargePoint(post.chargeAmount())))
                .build();
    }
}