package hhplus.ecommerce.server.interfaces.web.order.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSlicingSearchCond {

    private Integer size;
    private Long lastSequence;
    private LocalDateTime searchStartDateTime;
    private LocalDateTime searchEndDateTime;
}
