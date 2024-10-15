package hhplus.ecommerce.server.unit.item;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.OutOfItemStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ItemStockTest {

    @DisplayName("재고를 차감할 수 있다.")
    @Test
    void deductStock() {
        // given
        int leftAmount = 10;
        int deductAmount = 5;

        ItemStock itemStock = ItemStock.builder()
                .amount(leftAmount)
                .item(buildItem())
                .build();

        // when
        itemStock.deductStock(deductAmount);

        // then
        assertThat(itemStock.getAmount()).isEqualTo(leftAmount - deductAmount);
    }

    @DisplayName("재고가 부족할 경우 예외가 발생한다.")
    @Test
    void deductStockOutOfStock() {
        // given
        int leftAmount = 10;
        int deductAmount = 15;

        ItemStock itemStock = ItemStock.builder()
                .amount(leftAmount)
                .item(buildItem())
                .build();

        // when
        // then
        assertThatThrownBy(() -> itemStock.deductStock(deductAmount))
                .isInstanceOf(OutOfItemStockException.class)
                .hasMessage(new OutOfItemStockException(leftAmount).getMessage());
    }

    @DisplayName("재고가 부족한지를 확인할 수 있다.")
    @Test
    void throwOutOfItemStockException() {
        // given
        int leftAmount = 10;
        int deductAmount = 15;

        ItemStock itemStock = ItemStock.builder()
                .amount(leftAmount)
                .item(buildItem())
                .build();

        // when
        // then
        assertThatThrownBy(() -> itemStock.checkStock(deductAmount))
                .isInstanceOf(OutOfItemStockException.class)
                .hasMessage(new OutOfItemStockException(leftAmount).getMessage());
    }

    private Item buildItem() {
        return Item.builder()
                .build();
    }
}
