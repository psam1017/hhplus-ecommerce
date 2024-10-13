package hhplus.ecommerce.server.domain.cart;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.order.Order;
import hhplus.ecommerce.server.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "carts")
@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Builder
    protected Cart(int quantity, User user, Item item) {
        this.quantity = quantity;
        this.user = user;
        this.item = item;
    }
}
