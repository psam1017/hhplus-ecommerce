package hhplus.ecommerce.server.infrastructure.user;

import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.domain.user.repository.UserQueryRepository;
import hhplus.ecommerce.server.infrastructure.user.jpa.UserQueryDslRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Repository
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final UserQueryDslRepository userQueryDslRepository;

    @Override
    public User getById(Long id) {
        return null;
    }
}
