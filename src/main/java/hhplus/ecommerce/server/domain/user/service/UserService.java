package hhplus.ecommerce.server.domain.user.service;

import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.domain.user.exception.NoSuchUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class UserService {

    private final UserRepository userRepository;

    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(NoSuchUserException::new);
    }
}
