package hhplus.ecommerce.server.infrastructure.persistence.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.domain.user.exception.NoSuchUserException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserQueryDslRepository {

    private final EntityManager em;
    private final JPAQueryFactory query;

    public User getById(Long id) {
        User entity = em.find(User.class, id);
        if (entity == null) {
            throw new NoSuchUserException(id);
        }
        return entity;
    }
}
