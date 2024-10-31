package hhplus.ecommerce.server.domain.point;

import hhplus.ecommerce.server.domain.point.exception.OutOfPointException;
import hhplus.ecommerce.server.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "points")
@Entity
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Version
    private Long version;

    @Builder
    protected Point(Long id, int amount, User user) {
        this.id = id;
        this.amount = amount;
        this.user = user;
    }

    public void charge(int amount) {
        this.amount += amount;
    }

    public void usePoint(int amount) {
        if (this.amount < amount) {
            throw new OutOfPointException(this.amount);
        }
        this.amount -= amount;
    }
}
