package hhplus.ecommerce.server.integration.domain.cart;

import hhplus.ecommerce.server.domain.cart.Cart;
import hhplus.ecommerce.server.domain.cart.exception.NoSuchCartException;
import hhplus.ecommerce.server.domain.cart.service.CartService;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.repository.cart.CartJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.user.UserJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class CartServiceTest extends TestContainerEnvironment {

    @Autowired
    CartService sut;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    ItemJpaRepository itemJpaRepository;

    @Autowired
    CartJpaRepository cartJpaRepository;

    @DisplayName("새로운 상품을 장바구니에 추가할 수 있다.")
    @Test
    void addNewItemToCart() {
        // given
        User user = createUser("testUser");
        Item item = createItem("testItem", 1000);

        Cart newCart = createCart(1, user, item);

        // when
        Cart result = sut.putItem(newCart);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(user.getId());
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getQuantity()).isEqualTo(newCart.getQuantity());
    }

    @DisplayName("기존 상품의 수량을 변경할 수 있다.")
    @Test
    void updateItemQuantityInCart() {
        // given
        User user = createUser("testUser");
        Item item = createItem("testItem", 1000);

        Cart existingCart = createCart(2, user, item);
        cartJpaRepository.save(existingCart);

        Cart updatedCart = createCart(5, user, item);

        // when
        Cart result = sut.putItem(updatedCart);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(updatedCart.getQuantity());
    }

    @DisplayName("사용자의 장바구니 아이템을 조회할 수 있다.")
    @Test
    void getCartItems() {
        // given
        User user = createUser("testUser");
        Item item1 = createItem("testItem1", 1000);
        Item item2 = createItem("testItem2", 2000);

        Cart cart1 = createCart(1, user, item1);
        Cart cart2 = createCart(2, user, item2);
        cartJpaRepository.save(cart1);
        cartJpaRepository.save(cart2);

        // when
        List<Cart> result = sut.getCartItems(user.getId());

        // then
        assertThat(result).hasSize(2)
                .extracting("user.id", "item.id", "quantity")
                .containsExactly(
                        tuple(user.getId(), item1.getId(), cart1.getQuantity()),
                        tuple(user.getId(), item2.getId(), cart2.getQuantity())
                );
    }

    @DisplayName("장바구니에서 특정 아이템을 삭제할 수 있다.")
    @Test
    void deleteCartItem() {
        // given
        User user = createUser("testUser");
        Item item = createItem("testItem", 1000);

        Cart cart = createCart(1, user, item);
        Cart savedCart = cartJpaRepository.save(cart);

        // when
        Long result = sut.deleteCartItem(user.getId(), item.getId());

        // then
        assertThat(result).isEqualTo(savedCart.getId());
    }

    @DisplayName("장바구니에서 삭제할 아이템이 없을 경우 예외가 발생한다.")
    @Test
    void throwNoSuchCartExceptionWhenDeleteCartItem() {
        // given
        Long nonExistentUserId = 999L;
        Long nonExistentItemId = 888L;

        // when
        // then
        assertThatThrownBy(() -> sut.deleteCartItem(nonExistentUserId, nonExistentItemId))
                .isInstanceOf(NoSuchCartException.class)
                .hasMessage(new NoSuchCartException().getMessage());
    }

    @DisplayName("사용자의 장바구니에서 여러 아이템을 삭제할 수 있다.")
    @Test
    void deleteCartItems() {
        // given
        User user = createUser("testUser");
        Item item1 = createItem("testItem1", 1000);
        Item item2 = createItem("testItem2", 2000);

        Cart cart1 = createCart(1, user, item1);
        Cart cart2 = createCart(2, user, item2);
        cartJpaRepository.save(cart1);
        cartJpaRepository.save(cart2);

        Set<Long> itemIds = Set.of(item1.getId(), item2.getId());

        // when
        sut.deleteCartItems(user.getId(), itemIds);

        // then
        List<Cart> result = sut.getCartItems(user.getId());
        assertThat(result).isEmpty();
    }

    private User createUser(String username) {
        User user = User.builder()
                .username(username)
                .build();
        return userJpaRepository.save(user);
    }

    private Item createItem(String name, int price) {
        Item item = Item.builder()
                .name(name)
                .price(price)
                .build();
        return itemJpaRepository.save(item);
    }

    private Cart createCart(int quantity, User user, Item item) {
        return Cart.builder()
                .quantity(quantity)
                .user(user)
                .item(item)
                .build();
    }
}