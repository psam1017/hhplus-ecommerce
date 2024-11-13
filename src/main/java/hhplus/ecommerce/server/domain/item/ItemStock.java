package hhplus.ecommerce.server.domain.item;

import hhplus.ecommerce.server.domain.item.exception.OutOfItemStockException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "item_stocks",
        indexes = {
                @Index(name = "idx_item_stocks_item_id", columnList = "item_id", unique = true)
        }
)
@Entity
public class ItemStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;

    @OneToOne
    @JoinColumn(
            name = "item_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
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

    public void addStock(int amount) {
        this.amount += amount;
    }
}
