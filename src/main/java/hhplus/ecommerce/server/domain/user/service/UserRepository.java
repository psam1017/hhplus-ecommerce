package hhplus.ecommerce.server.domain.user.service;

import hhplus.ecommerce.server.domain.user.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long userId);
}
