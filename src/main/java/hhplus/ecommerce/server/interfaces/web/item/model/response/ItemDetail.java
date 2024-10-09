package hhplus.ecommerce.server.interfaces.web.item.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemDetail {

    private Long id;
    private String name;
    private Integer price;
    private Integer amount;
    private String description;

    @Builder
    protected ItemDetail(Long id, String name, Integer price, Integer amount, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.description = description;
    }
}
