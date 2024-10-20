package hhplus.ecommerce.server.unit.domain.cart;

import hhplus.ecommerce.server.domain.cart.Cart;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.OutOfItemStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CartTest {

    @DisplayName("장바구니의 수량을 대체할 수 있다.")
    @Test
    void putQuantity() {
        // given
        int leftQuantity = 10;
        int putQuantity = 5;

        Cart cart = Cart.builder()
                .quantity(10)
                .build();

        // when
        cart.putQuantity(putQuantity);

        // then
        assertThat(cart.getQuantity()).isEqualTo(putQuantity);
    }
}
