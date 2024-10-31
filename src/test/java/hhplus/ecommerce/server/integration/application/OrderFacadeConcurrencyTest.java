package hhplus.ecommerce.server.integration.application;

import hhplus.ecommerce.server.application.OrderFacade;
import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.OutOfItemStockException;
import hhplus.ecommerce.server.domain.order.service.OrderCommand;
import hhplus.ecommerce.server.domain.order.service.OrderService;
import hhplus.ecommerce.server.domain.point.Point;
import hhplus.ecommerce.server.domain.user.User;
import hhplus.ecommerce.server.infrastructure.data.OrderDataPlatform;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.item.ItemStockJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderItemJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.order.OrderJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.point.PointJpaRepository;
import hhplus.ecommerce.server.infrastructure.repository.user.UserJpaRepository;
import hhplus.ecommerce.server.integration.TestContainerEnvironment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class OrderFacadeConcurrencyTest extends TestContainerEnvironment {

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

    @DisplayName("동시에 발생한 30번의 주문 요청을 충돌 없이 처리할 수 있다.")
    @Test
    void createOrder_pessimisticWriteLock() throws InterruptedException {
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

        for (User user : users) {
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

    @DisplayName("동시에 여러 상품을 주문하더라도 교착상태에 빠지지 않는다.")
    @Test
    void createOrder_preventDeadLock() throws InterruptedException {
        // given
        int stockAmount = 10;
        int tryCount = 10;

        Item itemA = createItem("itemA", 1000);
        createItemStock(stockAmount, itemA);
        Item itemB = createItem("itemB", 1000);
        createItemStock(stockAmount, itemB);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < tryCount; i++) {
            User user = createUser("testUser" + i);
            createPoint(2000, user);
            users.add(user);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(tryCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(tryCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            int j = i;
            executorService.execute(() -> {
                try {
                    OrderCommand.CreateOrder command;
                    // 일부러 교착상태에 빠질 수 있는 상황을 유도하기 위해 한번은 오름차순, 한번은 내림차순으로 주문을 요청한다.
                    if (j % 2 == 0) {
                        command = new OrderCommand.CreateOrder(
                                user.getId(),
                                List.of(
                                        new OrderCommand.CreateOrderItem(itemA.getId(), 1),
                                        new OrderCommand.CreateOrderItem(itemB.getId(), 1)
                                )
                        );
                    } else {
                        command = new OrderCommand.CreateOrder(
                                user.getId(),
                                List.of(
                                        new OrderCommand.CreateOrderItem(itemB.getId(), 1),
                                        new OrderCommand.CreateOrderItem(itemA.getId(), 1)
                                )
                        );
                    }
                    startLatch.await();
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
        assertThat(successCount.get()).isEqualTo(tryCount);
        assertThat(failCount.get()).isEqualTo(0);
        List<ItemStock> itemStocks = itemStockJpaRepository.findAll();
        assertThat(itemStocks)
                .hasSize(2)
                .extracting(ItemStock::getAmount)
                .containsExactly(0, 0);
    }

    private User createUser(String username) {
        return userJpaRepository.save(User.builder()
                .username(username)
                .build());
    }

    private void createPoint(int amount, User user) {
        pointJpaRepository.save(Point.builder()
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

    private void createItemStock(int amount, Item item) {
        itemStockJpaRepository.save(ItemStock.builder()
                .amount(amount)
                .item(item)
                .build());
    }
}
