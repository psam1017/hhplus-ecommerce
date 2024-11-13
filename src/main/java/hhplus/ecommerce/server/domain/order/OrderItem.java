package hhplus.ecommerce.server.domain.order;

import hhplus.ecommerce.server.domain.item.Item;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "order_items",
        indexes = {
                @Index(name = "idx_order_items_order_id", columnList = "order_id"),
                @Index(name = "idx_order_items_item_id", columnList = "item_id"),
                @Index(name = "idx_order_items_composite", columnList = "order_id, item_id, total_amount")
        }
)
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    private int quantity;
    private int totalAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "order_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "item_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Item item;

    @Builder
    protected OrderItem(Long id, String name, int price, int quantity, Order order, Item item) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.totalAmount = price * quantity;
        this.order = order;
        this.item = item;
    }
}
