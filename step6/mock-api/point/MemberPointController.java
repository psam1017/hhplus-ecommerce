package hhplus.ecommerce.server.interfaces.point;

import hhplus.ecommerce.server.application.common.slicing.SlicingInfo;
import hhplus.ecommerce.server.interfaces.point.model.requqest.PointChargePost;
import hhplus.ecommerce.server.interfaces.point.model.requqest.PointHistorySlicingSearchCond;
import hhplus.ecommerce.server.interfaces.common.argument.UserId;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import hhplus.ecommerce.server.interfaces.point.model.response.PointHistoryDetail;
import hhplus.ecommerce.server.interfaces.point.model.response.PointDetail;
import hhplus.ecommerce.server.interfaces.point.model.response.PointHistorySlicingResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping("/api/members/points")
@RestController
public class MemberPointController {

    @GetMapping("")
    public ApiResponse<PointDetail> getPoint(
            @UserId Long userId
    ) {
        PointDetail response = PointDetail.builder()
                .amount(1000)
                .build();
        return ApiResponse.ok(response);
    }

    @PostMapping("/charge")
    public ApiResponse<PointDetail> chargePoint(
            @UserId Long userId,
            @RequestBody @Valid PointChargePost post
    ) {
        PointDetail response = PointDetail.builder()
                .amount(2000)
                .build();
        return ApiResponse.ok(response);
    }

    @Operation(
            parameters = {
                    @Parameter(name = "size"),
                    @Parameter(name = "lastSequence"),
                    @Parameter(name = "searchStartDateTime"),
                    @Parameter(name = "searchEndDateTime")
            }
    )
    @GetMapping("/histories")
    public ApiResponse<PointHistorySlicingResult> getPointHistories(
            @UserId Long userId,
            @Parameter(hidden = true) @ModelAttribute @Valid PointHistorySlicingSearchCond cond
    ) {
        List<PointHistoryDetail> responses = List.of(
                PointHistoryDetail.builder()
                        .originalBalance(0)
                        .changeAmount(1000)
                        .reason("포인트 충전")
                        .createdDateTime(LocalDateTime.now().minusDays(3))
                        .build(),
                PointHistoryDetail.builder()
                        .originalBalance(1000)
                        .changeAmount(-500)
                        .reason("상품 구매: 상품1 외 2건")
                        .createdDateTime(LocalDateTime.now().minusDays(2))
                        .build(),
                PointHistoryDetail.builder()
                        .originalBalance(500)
                        .changeAmount(2000)
                        .reason("포인트 충전")
                        .createdDateTime(LocalDateTime.now().minusDays(1))
                        .build()
        );

        PointHistorySlicingResult result = PointHistorySlicingResult.builder()
                .pointHistories(responses)
                .slicingInfo(new SlicingInfo(10, 130, true))
                .build();

        return ApiResponse.ok(result);
    }
}
