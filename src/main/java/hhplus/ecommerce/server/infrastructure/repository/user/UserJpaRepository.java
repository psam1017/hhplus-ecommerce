package hhplus.ecommerce.server.infrastructure.repository.user;

import hhplus.ecommerce.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long> {
}
