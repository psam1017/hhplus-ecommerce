package hhplus.ecommerce.server.domain.user.repository;

import hhplus.ecommerce.server.domain.user.User;

public interface UserQueryRepository {

    User getById(Long id);
}
