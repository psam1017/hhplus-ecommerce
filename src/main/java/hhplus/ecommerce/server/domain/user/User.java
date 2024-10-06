package hhplus.ecommerce.server.domain.user;

import hhplus.ecommerce.server.domain.user.enumeration.UserRole;
import hhplus.ecommerce.server.domain.user.enumeration.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private String refreshToken;

    @Builder
    protected User(String username, String password, UserStatus status, UserRole role, String refreshToken) {
        this.username = username;
        this.password = password;
        this.status = status;
        this.role = role;
        this.refreshToken = refreshToken;
    }

    public void renewRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void removeRefreshToken() {
        this.refreshToken = null;
    }
}
