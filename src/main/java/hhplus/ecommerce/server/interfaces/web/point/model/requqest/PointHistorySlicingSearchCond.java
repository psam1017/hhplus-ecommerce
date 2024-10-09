package hhplus.ecommerce.server.interfaces.web.point.model.requqest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistorySlicingSearchCond {

    private Integer size;
    private Long lastSequence;
    private LocalDateTime searchStartDateTime;
    private LocalDateTime searchEndDateTime;
}
