package hhplus.ecommerce.server.domain.cart;

public class CartInfo {

    public record CartDetail(
            Long id,
            Long itemId,
            String name,
            Integer price,
            Integer amount
    ) {
    }
}
