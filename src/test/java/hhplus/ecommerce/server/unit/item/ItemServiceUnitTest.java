package hhplus.ecommerce.server.unit.item;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemException;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemStockException;
import hhplus.ecommerce.server.domain.item.service.ItemRepository;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.item.service.ItemStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {

    @InjectMocks
    ItemService sut;

    @Mock
    ItemRepository itemRepository;

    @Mock
    ItemStockRepository itemStockRepository;

    @DisplayName("가장 인기 있는 상품들을 조회할 수 있다.")
    @Test
    void findTopItems() {
        // given
        Long id1 = 1L;
        String name1 = "Test Item1";
        int price1 = 1000;
        Long id2 = 2L;
        String name2 = "Test Item2";
        int price2 = 1000;

        LocalDateTime endDateTime = LocalDate.now().atStartOfDay();
        LocalDateTime startDateTime = endDateTime.minusDays(3);

        List<Item> topItems = List.of(
                buildItem(id1, name1, price1),
                buildItem(id2, name2, price2)
        );

        when(itemRepository.findTopItems(startDateTime, endDateTime)).thenReturn(topItems);

        // when
        List<Item> result = sut.findTopItems();

        // then
        assertThat(result).hasSize(2)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(
                        tuple(id1, name1, price1),
                        tuple(id2, name2, price2)
                );
        verify(itemRepository, times(1)).findTopItems(startDateTime, endDateTime);
    }

    @DisplayName("모든 상품을 조회할 수 있다.")
    @Test
    void findItems() {
        // given
        Long id1 = 1L;
        String name1 = "Test Item1";
        int price1 = 1000;
        Long id2 = 2L;
        String name2 = "Test Item2";
        int price2 = 1000;
        List<Item> items = List.of(
                buildItem(id1, name1, price1),
                buildItem(id2, name2, price2)
        );

        when(itemRepository.findAll()).thenReturn(items);

        // when
        List<Item> result = sut.findItems();

        // then
        assertThat(result).hasSize(2)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(
                        tuple(id1, name1, price1),
                        tuple(id2, name2, price2)
                );
        verify(itemRepository, times(1)).findAll();
    }

    @DisplayName("특정 아이디들의 상품을 조회할 수 있다.")
    @Test
    void findItemsByIds() {
        // given
        Long id1 = 1L;
        String name1 = "Test Item1";
        int price1 = 1000;
        Long id2 = 2L;
        String name2 = "Test Item2";
        int price2 = 1000;

        Set<Long> itemIds = Set.of(id1, id2);
        List<Item> items = List.of(
                buildItem(id1, name1, price1),
                buildItem(id2, name2, price2)
        );

        when(itemRepository.findAllById(itemIds)).thenReturn(items);

        // when
        List<Item> result = sut.findItems(itemIds);

        // then
        assertThat(result).hasSize(2)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(
                        tuple(id1, name1, price1),
                        tuple(id2, name2, price2)
                );
        verify(itemRepository, times(1)).findAllById(itemIds);
    }

    @DisplayName("존재하지 않는 아이디로 상품을 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemExceptionWhenFindItemsByIds() {
        // given
        Long id = 1L;
        String name = "Test Item";
        int price = 1000;
        Long notExistId = 3L;

        Set<Long> itemIds = Set.of(id, notExistId);
        List<Item> items = List.of(buildItem(id, name, price));

        when(itemRepository.findAllById(itemIds)).thenReturn(items);

        NoSuchItemException exception = new NoSuchItemException();

        // when
        // then
        assertThatThrownBy(() -> sut.findItems(itemIds))
                .isInstanceOf(exception.getClass())
                .hasMessage(exception.getMessage());
        verify(itemRepository, times(1)).findAllById(itemIds);
    }

    @DisplayName("락을 걸고 상품 재고를 조회할 수 있다.")
    @Test
    void getItemStockWithLock() {
        // given
        Long id1 = 1L;
        String name1 = "Test Item";
        int price1 = 1000;
        int amount1 = 10;
        Long id2 = 2L;
        String name2 = "Test Item";
        int price2 = 1000;
        int amount2 = 10;
        Set<Long> itemIds = Set.of(id1, id2);
        List<ItemStock> itemStocks = List.of(
                buildItemStock(id1, buildItem(id1, name1, price1), amount1),
                buildItemStock(id2, buildItem(id2, name2, price2), amount2)
        );

        when(itemStockRepository.findAllByItemIdWithLock(itemIds)).thenReturn(itemStocks);

        // when
        List<ItemStock> result = sut.getItemStocksWithLock(itemIds);

        // then
        assertThat(result).hasSize(2)
                .extracting(i -> tuple(i.getId(), i.getItem().getId(), i.getItem().getName(), i.getItem().getPrice(), i.getAmount()))
                .containsExactly(
                        tuple(id1, id1, name1, price1, amount1),
                        tuple(id2, id2, name2, price2, amount2)
                );
        verify(itemStockRepository, times(1)).findAllByItemIdWithLock(itemIds);
    }

    @DisplayName("존재하지 않는 아이디로 락을 걸면서 상품 재고를 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemExceptionWhenGetItemStockWithLock() {
        // given
        Long id = 1L;
        String name = "Test Item";
        int price = 1000;
        int amount = 10;
        Long notExistId = 3L;
        Set<Long> itemIds = Set.of(id, notExistId);
        List<ItemStock> itemStocks = List.of(buildItemStock(id, buildItem(id, name, price), amount));

        when(itemStockRepository.findAllByItemIdWithLock(itemIds)).thenReturn(itemStocks);

        NoSuchItemStockException exception = new NoSuchItemStockException();

        // when
        // then
        assertThatThrownBy(() -> sut.getItemStocksWithLock(itemIds))
                .isInstanceOf(exception.getClass())
                .hasMessage(exception.getMessage());
        verify(itemStockRepository, times(1)).findAllByItemIdWithLock(itemIds);
    }

    @DisplayName("상품을 조회할 수 있다.")
    @Test
    void getItem() {
        // given
        Long id = 1L;
        String name = "Test Item";
        int price = 1000;
        Item item = buildItem(id, name, price);

        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        // when
        Item result = sut.getItem(id);

        // then
        assertThat(result).extracting(Item::getId, Item::getName, Item::getPrice)
                .containsExactly(id, name, price);
        verify(itemRepository, times(1)).findById(id);
    }

    @DisplayName("존재하지 않는 아이디로 상품을 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemExceptionWhenGetItem() {
        // given
        Long id = 1L;

        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        NoSuchItemException exception = new NoSuchItemException();

        // when
        // then
        assertThatThrownBy(() -> sut.getItem(id))
                .isInstanceOf(exception.getClass())
                .hasMessage(exception.getMessage());
        verify(itemRepository, times(1)).findById(id);
    }

    @DisplayName("여러 상품의 재고를 조회할 수 있다.")
    @Test
    void getStocks() {
        // given
        Long id1 = 1L;
        String name1 = "Test Item1";
        int price1 = 1000;
        int amount1 = 10;
        Long id2 = 2L;
        String name2 = "Test Item2";
        int price2 = 1000;
        int amount2 = 10;
        Set<Long> itemIds = Set.of(id1, id2);
        List<ItemStock> itemStocks = List.of(
                buildItemStock(id1, buildItem(id1, name1, price1), amount1),
                buildItemStock(id2, buildItem(id2, name2, price2), amount2)
        );

        when(itemStockRepository.findAllByItemIds(itemIds)).thenReturn(itemStocks);

        // when
        Map<Long, Integer> stocks = sut.getStocks(itemIds);

        // then
        assertThat(stocks).hasSize(2)
                .containsEntry(id1, amount1)
                .containsEntry(id2, amount2);
        verify(itemStockRepository, times(1)).findAllByItemIds(itemIds);
    }

    @DisplayName("존재하지 않는 아이디로 상품 재고를 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemStockExceptionWhenGetStocks() {
        // given
        Long id = 1L;
        String name = "Test Item";
        int price = 1000;
        int amount = 10;
        Long notExistId = 3L;
        Set<Long> itemIds = Set.of(id, notExistId);
        List<ItemStock> itemStocks = List.of(buildItemStock(id, buildItem(id, name, price), amount));

        when(itemStockRepository.findAllByItemIds(itemIds)).thenReturn(itemStocks);

        NoSuchItemStockException exception = new NoSuchItemStockException();

        // when
        // then
        assertThatThrownBy(() -> sut.getStocks(itemIds))
                .isInstanceOf(exception.getClass())
                .hasMessage(exception.getMessage());
        verify(itemStockRepository, times(1)).findAllByItemIds(itemIds);
    }

    @DisplayName("상품 재고를 조회할 수 있다.")
    @Test
    void getItemStockByItemId() {
        // given
        Long id = 1L;
        Long itemId = 2L;
        String name = "Test Item";
        int price = 1000;
        int amount = 10;
        ItemStock itemStock = buildItemStock(id, buildItem(itemId, name, price), amount);

        when(itemStockRepository.findByItemId(itemId)).thenReturn(Optional.of(itemStock));

        // when
        ItemStock result = sut.getItemStockByItemId(itemId);

        // then
        assertThat(result)
                .extracting(is -> tuple(is.getId(), is.getItem().getId(), is.getItem().getName(), is.getItem().getPrice(), is.getAmount()))
                .isEqualTo(tuple(id, itemId, name, price, amount));
        verify(itemStockRepository, times(1)).findByItemId(itemId);
    }

    @DisplayName("존재하지 않는 아이디로 상품 재고를 조회할 경우 예외가 발생한다.")
    @Test
    void throwNoSuchItemStockExceptionWhenGetItemStockByItemId() {
        // given
        Long id = 1L;

        when(itemStockRepository.findByItemId(id)).thenReturn(Optional.empty());

        NoSuchItemStockException exception = new NoSuchItemStockException();

        // when
        // then
        assertThatThrownBy(() -> sut.getItemStockByItemId(id))
                .isInstanceOf(exception.getClass())
                .hasMessage(exception.getMessage());
        verify(itemStockRepository, times(1)).findByItemId(id);
    }

    private Item buildItem(Long id, String name, int price) {
        return Item.builder().id(id).name(name).price(price).build();
    }

    private ItemStock buildItemStock(Long id, Item item, int amount) {
        return ItemStock.builder().id(id).amount(amount).item(item).build();
    }
}
