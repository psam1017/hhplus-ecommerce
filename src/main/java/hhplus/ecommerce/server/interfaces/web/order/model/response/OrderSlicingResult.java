package hhplus.ecommerce.server.interfaces.web.order.model.response;

import hhplus.ecommerce.server.application.common.model.slicing.SlicingInfo;
import hhplus.ecommerce.server.interfaces.web.point.model.response.PointHistoryDetail;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderSlicingResult {

    private SlicingInfo slicingInfo;
    private List<OrderSummary> orders;

    @Builder
    protected OrderSlicingResult(SlicingInfo slicingInfo, List<OrderSummary> orders) {
        this.slicingInfo = slicingInfo;
        this.orders = orders;
    }
}
