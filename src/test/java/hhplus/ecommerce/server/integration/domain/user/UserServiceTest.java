package hhplus.ecommerce.server.integration.domain.user;

import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.domain.user.exception.NoSuchUserException;
import hhplus.ecommerce.server.domain.user.service.UserService;
import hhplus.ecommerce.server.infrastructure.user.UserJpaRepository;
import hhplus.ecommerce.server.integration.domain.ServiceTestEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserServiceTest extends ServiceTestEnvironment {

    @Autowired
    UserService sut;

    @Autowired
    UserJpaRepository userJpaRepository;

    @DisplayName("사용자를 조회할 수 있다.")
    @Test
    void getUser() {
        // given
        User savedUser = createUser("testUser");
        userJpaRepository.save(savedUser);

        // when
        User result = sut.getUser(savedUser.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedUser.getId());
        assertThat(result.getUsername()).isEqualTo(savedUser.getUsername());
    }

    @DisplayName("사용자가 존재하지 않을 경우 예외가 발생한다.")
    @Test
    void throwNoSuchUserException() {
        // given
        Long nonExistentUserId = 999L;

        // when
        // then
        assertThatThrownBy(() -> sut.getUser(nonExistentUserId))
                .isInstanceOf(NoSuchUserException.class)
                .hasMessage(new NoSuchUserException().getMessage());
    }

    private User createUser(String username) {
        return User.builder()
                .username(username)
                .build();
    }
}