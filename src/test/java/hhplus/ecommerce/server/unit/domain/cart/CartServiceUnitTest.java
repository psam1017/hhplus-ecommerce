package hhplus.ecommerce.server.unit.domain.cart;

import hhplus.ecommerce.server.domain.cart.Cart;
import hhplus.ecommerce.server.domain.cart.exception.NoSuchCartException;
import hhplus.ecommerce.server.domain.cart.service.CartRepository;
import hhplus.ecommerce.server.domain.cart.service.CartService;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {

    @InjectMocks
    CartService sut;

    @Mock
    CartRepository cartRepository;

    @DisplayName("새로운 상품을 추가할 수 있다.")
    @Test
    void addNewItemToCart() {
        // given
        Long userId = 1L;
        Long itemId = 1L;

        Cart newCart = Cart.builder()
                .user(buildUser(userId))
                .item(buildItem(itemId))
                .quantity(1)
                .build();

        when(cartRepository.findByUserIdAndItemId(userId, itemId))
                .thenReturn(Optional.empty());
        when(cartRepository.save(newCart)).thenReturn(newCart);

        // when
        Cart result = sut.putItem(newCart);

        // then
        assertThat(result).isEqualTo(newCart);
        verify(cartRepository, times(1)).findByUserIdAndItemId(userId, itemId);
        verify(cartRepository, times(1)).save(newCart);
    }

    @DisplayName("기존의 상품 수량을 변경할 수 있다.")
    @Test
    void updateItemQuantityInCart() {
        // given
        Long userId = 1L;
        Long itemId = 1L;

        Cart newCart = Cart.builder()
                .user(buildUser(userId))
                .item(buildItem(itemId))
                .quantity(1)
                .build();
        Cart existingCart = Cart.builder()
                .user(buildUser(userId))
                .item(buildItem(itemId))
                .quantity(2)
                .build();

        when(cartRepository.findByUserIdAndItemId(userId, itemId))
                .thenReturn(Optional.of(existingCart));

        // when
        Cart result = sut.putItem(newCart);

        // then
        assertThat(result.getQuantity()).isEqualTo(newCart.getQuantity());
        verify(cartRepository, times(1)).findByUserIdAndItemId(userId, itemId);
    }

    @DisplayName("사용자의 모든 장바구니 아이템을 조회할 수 있다.")
    @Test
    void getCartItems() {
        // given
        Long userId = 1L;

        Cart cart1 = Cart.builder().build();
        Cart cart2 = Cart.builder().build();
        List<Cart> cartItems = List.of(
                cart1,
                cart2
        );

        when(cartRepository.findAllByUserId(eq(userId)))
                .thenReturn(cartItems);

        // when
        List<Cart> result = sut.getCartItems(userId);

        // then
        assertThat(result).hasSize(2)
                .containsExactly(cart1, cart2);
        verify(cartRepository, times(1)).findAllByUserId(userId);
    }

    @DisplayName("장바구니 아이디로 장바구니 아이템을 조회할 수 있다.")
    @Test
    void getCartItemsByIds() {
        // given
        Long userId = 1L;

        Cart cart1 = Cart.builder().build();
        Cart cart2 = Cart.builder().build();
        List<Cart> cartItems = List.of(
                cart1,
                cart2
        );

        when(cartRepository.findAllByUserIdAndIdIn(eq(userId), anySet()))
                .thenReturn(cartItems);

        // when
        List<Cart> result = sut.getCartItems(userId, Set.of(1L, 2L));

        // then
        assertThat(result).hasSize(2)
                .containsExactly(cart1, cart2);
        verify(cartRepository, times(1)).findAllByUserIdAndIdIn(userId, Set.of(1L, 2L));
    }

    @DisplayName("존재하지 않는 장바구니 아이디를 전송하면 조회 실패로 예외가 발생한다.")
    @Test
    void throwNoSuchCartExceptionWhenGetCartItemsByIds() {
        // given
        Long userId = 1L;

        when(cartRepository.findAllByUserIdAndIdIn(eq(userId), anySet()))
                .thenReturn(List.of());

        // when
        // then
        assertThatThrownBy(() -> sut.getCartItems(userId, Set.of(1L, 2L)))
                .isInstanceOf(NoSuchCartException.class)
                .hasMessage(new NoSuchCartException().getMessage());
    }

    @DisplayName("장바구니에서 특정 아이템을 삭제할 수 있다.")
    @Test
    void deleteCartItem() {
        // given
        Long userId = 1L;
        Long itemId = 1L;

        Cart cart = Cart.builder()
                .user(buildUser(userId))
                .item(buildItem(itemId))
                .build();

        when(cartRepository.findByUserIdAndItemId(userId, itemId))
                .thenReturn(Optional.of(cart));

        // when
        Long result = sut.deleteCartItem(userId, itemId);

        // then
        assertThat(result).isEqualTo(cart.getId());
        verify(cartRepository, times(1)).findByUserIdAndItemId(userId, itemId);
        verify(cartRepository, times(1)).delete(cart);
    }

    @DisplayName("장바구니에서 삭제할 아이템이 없을 경우 예외가 발생한다.")
    @Test
    void throwNoSuchCartExceptionWhenDeleteCartItem() {
        // given
        Long userId = 1L;
        Long itemId = 1L;

        when(cartRepository.findByUserIdAndItemId(userId, itemId))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> sut.deleteCartItem(userId, itemId))
                .isInstanceOf(NoSuchCartException.class)
                .hasMessage(new NoSuchCartException().getMessage());
        verify(cartRepository, times(1)).findByUserIdAndItemId(userId, itemId);
    }

    private User buildUser(Long userId) {
        return User.builder().id(userId).build();
    }

    private Item buildItem(Long itemId) {
        return Item.builder().id(itemId).build();
    }
}
