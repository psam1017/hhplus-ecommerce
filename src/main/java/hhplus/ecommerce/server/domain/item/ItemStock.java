package hhplus.ecommerce.server.domain.item;

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
    protected ItemStock(int amount, Item item) {
        this.amount = amount;
        this.item = item;
    }
}
