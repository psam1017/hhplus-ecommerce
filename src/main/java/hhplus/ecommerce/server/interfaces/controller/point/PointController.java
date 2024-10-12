package hhplus.ecommerce.server.interfaces.controller.point;

import hhplus.ecommerce.server.application.PointFacade;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/points")
@RestController
public class PointController {

    private final PointFacade pointFacade;

    @GetMapping("")
    public ApiResponse<PointDto.PointResponse> getPoint(
            @PathVariable Long userId
    ) {
        return ApiResponse.ok(
                PointDto.PointResponse.builder()
                        .point(pointFacade.getPoint(userId))
                        .build()
        );
    }

    @PostMapping("/charge")
    public ApiResponse<PointDto.PointResponse> chargePoint(
            @PathVariable Long userId,
            @RequestBody @Valid PointDto.PointCreate post
    ) {
        return ApiResponse.ok(
                PointDto.PointResponse.builder()
                        .point(pointFacade.chargePoint(userId, post))
                        .build()
        );
    }
}
