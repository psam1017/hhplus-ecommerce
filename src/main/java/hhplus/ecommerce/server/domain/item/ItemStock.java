package hhplus.ecommerce.server.domain.item;

import hhplus.ecommerce.server.domain.item.exception.OutOfItemStockException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "item_stocks")
@Entity
public class ItemStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    protected ItemStock(Long id, int amount, Item item) {
        this.id = id;
        this.amount = amount;
        this.item = item;
    }

    public void deductStock(Integer amount) {
        checkStock(amount);
        this.amount -= amount;
    }

    public void checkStock(Integer amount) {
        if (this.amount < amount) {
            throw new OutOfItemStockException(this.amount);
        }
    }
}
