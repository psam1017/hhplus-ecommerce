package hhplus.ecommerce.server.domain.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("status = 'ACTIVE'")
@Table(name = "items")
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int price;

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @Builder
    protected Item(Long id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.status = ItemStatus.ACTIVE;
    }
}
