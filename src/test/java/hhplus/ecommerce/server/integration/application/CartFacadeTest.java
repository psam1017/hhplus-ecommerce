package hhplus.ecommerce.server.integration.application;

import hhplus.ecommerce.server.application.CartFacade;
import hhplus.ecommerce.server.domain.cart.exception.NoSuchCartException;
import hhplus.ecommerce.server.domain.cart.service.CartCommand;
import hhplus.ecommerce.server.domain.cart.service.CartInfo;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.cart.CartJpaRepository;
import hhplus.ecommerce.server.infrastructure.item.ItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.item.ItemStockJpaRepository;
import hhplus.ecommerce.server.infrastructure.user.UserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class CartFacadeTest extends FacadeTestEnvironment {

    @Autowired
    CartFacade cartFacade;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    ItemJpaRepository itemJpaRepository;

    @Autowired
    ItemStockJpaRepository itemStockJpaRepository;

    @Autowired
    CartJpaRepository cartJpaRepository;

    @DisplayName("상품을 장바구니에 추가할 수 있다.")
    @Test
    void putItemInCart() {
        // given
        int stockAmount = 10;

        User user = createUser("testUser");
        Item item = createItem("testItem", 1000);
        createItemStock(stockAmount, item);
        CartCommand.PutItem command = new CartCommand.PutItem(user.getId(), item.getId(), stockAmount);

        // when
        CartInfo.CartDetail result = cartFacade.putItem(command);

        // then
        assertThat(result).isNotNull();

        assertThat(result.amount()).isEqualTo(stockAmount);
        assertThat(result.name()).isEqualTo(item.getName());
        assertThat(result.price()).isEqualTo(item.getPrice());
    }

    @DisplayName("장바구니의 아이템 목록을 조회할 수 있다.")
    @Test
    void getCartItems() {
        // given
        User user = createUser("testUser");
        Item item1 = createItem("testItem1", 1000);
        Item item2 = createItem("testItem2", 2000);
        createItemStock(10, item1);
        createItemStock(10, item2);
        cartFacade.putItem(new CartCommand.PutItem(user.getId(), item1.getId(), 1));
        cartFacade.putItem(new CartCommand.PutItem(user.getId(), item2.getId(), 2));

        // when
        List<CartInfo.CartDetail> result = cartFacade.getCartItems(user.getId());

        // then
        assertThat(result).hasSize(2)
                .extracting(c -> tuple(c.itemId(), c.name(), c.price(), c.amount()))
                .containsExactlyInAnyOrder(
                        tuple(item1.getId(), item1.getName(), item1.getPrice(), 1),
                        tuple(item2.getId(), item2.getName(), item2.getPrice(), 2)
                );
    }

    @DisplayName("장바구니에서 특정 아이템을 삭제할 수 있다.")
    @Test
    void deleteCartItem() {
        // given
        User user = createUser("testUser");
        Item item = createItem("testItem", 1000);
        createItemStock(10, item);
        CartInfo.CartDetail cartDetail = cartFacade.putItem(new CartCommand.PutItem(user.getId(), item.getId(), 1));

        // when
        Long deletedCartId = cartFacade.deleteCartItem(user.getId(), item.getId());

        // then
        assertThat(deletedCartId).isEqualTo(cartDetail.id());
    }

    private User createUser(String username) {
        return userJpaRepository.save(User.builder()
                .username(username)
                .build());
    }

    private Item createItem(String name, int price) {
        return itemJpaRepository.save(Item.builder()
                .name(name)
                .price(price)
                .build());
    }

    private ItemStock createItemStock(int amount, Item item) {
        return itemStockJpaRepository.save(ItemStock.builder()
                .amount(amount)
                .item(item)
                .build());
    }
}