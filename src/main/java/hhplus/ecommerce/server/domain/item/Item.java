package hhplus.ecommerce.server.domain.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "items")
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int price;

    @OneToOne(mappedBy = "item")
    private ItemStock itemStock;

    @Builder
    protected Item(String name, int price) {
        this.name = name;
        this.price = price;
    }
}
