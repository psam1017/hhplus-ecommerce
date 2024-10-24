package hhplus.ecommerce.server.domain.order;

import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders")
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    protected Order(Long id, OrderStatus status, LocalDateTime orderDateTime, User user) {
        this.id = id;
        this.status = status;
        this.orderDateTime = orderDateTime;
        this.user = user;
    }
}
