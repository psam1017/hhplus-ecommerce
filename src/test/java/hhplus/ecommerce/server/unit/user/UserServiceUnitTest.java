package hhplus.ecommerce.server.unit.user;

import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.domain.user.exception.NoSuchUserException;
import hhplus.ecommerce.server.domain.user.service.UserRepository;
import hhplus.ecommerce.server.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    @InjectMocks
    UserService sut;

    @Mock
    UserRepository userRepository;

    @DisplayName("사용자를 조회할 수 있다.")
    @Test
    void getUser() {
        // given
        Long userId = 1L;
        User user = User.builder().build();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when
        User result = sut.getUser(userId);

        // then
        assertThat(result).isEqualTo(user);
        verify(userRepository, times(1)).findById(userId);
    }

    @DisplayName("사용자가 존재하지 않을 경우 예외가 발생한다.")
    @Test
    void throwNoSuchUserException() {
        // given
        Long userId = 1L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> sut.getUser(userId))
                .isInstanceOf(NoSuchUserException.class)
                .hasMessage(new NoSuchUserException().getMessage());
        verify(userRepository, times(1)).findById(userId);
    }
}
