package hhplus.ecommerce.server.domain.item;

public class ItemInfo {

    public record ItemDetail(
            Long id,
            String name,
            Integer price,
            Integer amount
    ) {
    }
}
