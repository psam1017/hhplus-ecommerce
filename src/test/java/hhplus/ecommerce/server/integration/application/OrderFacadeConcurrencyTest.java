package hhplus.ecommerce.server.integration.application;

import hhplus.ecommerce.server.application.OrderFacade;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.OutOfItemStockException;
import hhplus.ecommerce.server.domain.order.service.OrderCommand;
import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.data.OrderDataPlatform;
import hhplus.ecommerce.server.infrastructure.item.ItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.item.ItemStockJpaRepository;
import hhplus.ecommerce.server.infrastructure.order.OrderItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.order.OrderJpaRepository;
import hhplus.ecommerce.server.infrastructure.point.PointJpaRepository;
import hhplus.ecommerce.server.infrastructure.user.UserJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderFacadeConcurrencyTest {

    @Autowired
    OrderFacade orderFacade;

    @Autowired
    UserJpaRepository userJpaRepository;

    @Autowired
    PointJpaRepository pointJpaRepository;

    @Autowired
    ItemJpaRepository itemJpaRepository;

    @Autowired
    ItemStockJpaRepository itemStockJpaRepository;

    @Autowired
    OrderJpaRepository orderJpaRepository;

    @Autowired
    OrderItemJpaRepository orderItemJpaRepository;

    @MockBean
    OrderDataPlatform orderDataPlatform;

    @AfterEach
    void tearDown() {
        orderItemJpaRepository.deleteAll();
        orderJpaRepository.deleteAll();
        itemStockJpaRepository.deleteAll();
        itemJpaRepository.deleteAll();
        pointJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @DisplayName("동시에 발생한 30번의 주문 요청을 충돌 없이 처리할 수 있다.")
    @Test
    void createOrder() throws InterruptedException {
        // given
        int stockAmount = 10;

        Item item = createItem("item1", 1000);
        createItemStock(stockAmount, item);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            User user = createUser("testUser");
            createPoint(1000, user);
            users.add(user);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(30);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(30);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < 30; i++) {
            User user = users.get(i);
            executorService.execute(() -> {
                try {
                    startLatch.await();
                    OrderCommand.CreateOrder command = new OrderCommand.CreateOrder(
                            user.getId(),
                            List.of(new OrderCommand.CreateOrderItem(item.getId(), 1))
                    );
                    orderFacade.createOrder(command);
                    successCount.incrementAndGet();
                } catch (OutOfItemStockException e) {
                    failCount.incrementAndGet();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // when
        startLatch.countDown();
        endLatch.await();

        // then
        assertThat(successCount.get()).isEqualTo(stockAmount);
        assertThat(failCount.get()).isEqualTo(30 - stockAmount);

        ItemStock itemStock = itemStockJpaRepository.findByItemId(item.getId()).orElseThrow();
        assertThat(itemStock.getAmount()).isEqualTo(0);

        verify(orderDataPlatform, times(stockAmount)).saveOrderData(anyMap());
    }

    private User createUser(String username) {
        return userJpaRepository.save(User.builder()
                .username(username)
                .build());
    }

    private Point createPoint(int amount, User user) {
        return pointJpaRepository.save(Point.builder()
                .amount(amount)
                .user(user)
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
