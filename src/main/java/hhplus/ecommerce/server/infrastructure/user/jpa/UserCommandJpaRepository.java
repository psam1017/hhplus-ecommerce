package hhplus.ecommerce.server.infrastructure.user.jpa;

import hhplus.ecommerce.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCommandJpaRepository extends JpaRepository<User, Long> {
}