package hhplus.ecommerce.server.interfaces.point.model.response;

import hhplus.ecommerce.server.interfaces.common.jsonformat.KoreanDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PointHistoryDetail {

    private Integer originalBalance;
    private Integer changeAmount;
    private String reason;
    @KoreanDateTime
    private LocalDateTime createdDateTime;

    @Builder
    protected PointHistoryDetail(Integer originalBalance, Integer changeAmount, String reason, LocalDateTime createdDateTime) {
        this.originalBalance = originalBalance;
        this.changeAmount = changeAmount;
        this.reason = reason;
        this.createdDateTime = createdDateTime;
    }
}
