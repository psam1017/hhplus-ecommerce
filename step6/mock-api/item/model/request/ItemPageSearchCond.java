package hhplus.ecommerce.server.interfaces.item.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPageSearchCond {

    private Integer page;
    private Integer size;
    private String prop;
    private String dir;
    private String search;
    private Integer atLeastPrice;
    private Integer atMostPrice;
}
