package hhplus.ecommerce.server.interfaces.web.item.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemSummary {

    private Long id;
    private String name;
    private Integer price;
    private Integer amount;

    @Builder
    protected ItemSummary(Long id, String name, Integer price, Integer amount) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.amount = amount;
    }
}
