package hhplus.ecommerce.server.interfaces.point.model.response;

import hhplus.ecommerce.server.application.common.slicing.SlicingInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PointHistorySlicingResult {

    private SlicingInfo slicingInfo;
    private List<PointHistoryDetail> pointHistories;

    @Builder
    protected PointHistorySlicingResult(SlicingInfo slicingInfo, List<PointHistoryDetail> pointHistories) {
        this.slicingInfo = slicingInfo;
        this.pointHistories = pointHistories;
    }
}
