package hhplus.ecommerce.server.infrastructure.user;

import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.domain.user.service.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Repository
public class UserRepositoryImpl implements UserRepository {



    public User getById(Long id) {
        return null;
    }
}
