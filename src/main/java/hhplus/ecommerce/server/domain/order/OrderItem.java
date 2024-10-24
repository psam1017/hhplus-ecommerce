package hhplus.ecommerce.server.domain.order;

import hhplus.ecommerce.server.domain.item.Item;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items")
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    protected OrderItem(Long id, String name, int price, int quantity, Order order, Item item) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.order = order;
        this.item = item;
    }
}
