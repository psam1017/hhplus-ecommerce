package hhplus.ecommerce.server.infrastructure.user.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import hhplus.ecommerce.server.domain.user.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserQueryDslRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;
}
