package hhplus.ecommerce.server.unit.domain.item;

import hhplus.ecommerce.server.domain.item.Item;
import hhplus.ecommerce.server.domain.item.ItemStock;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemException;
import hhplus.ecommerce.server.domain.item.exception.NoSuchItemStockException;
import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import hhplus.ecommerce.server.domain.item.service.ItemRepository;
import hhplus.ecommerce.server.domain.item.service.ItemService;
import hhplus.ecommerce.server.domain.item.service.ItemStockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {

    @InjectMocks
    ItemService sut;

    @Mock
    ItemRepository itemRepository;

    @Mock
    ItemStockRepository itemStockRepository;

    @DisplayName("상품을 페이징하여 조회할 수 있다.")
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

        when(itemRepository.findAllBySearchCond(any(ItemCommand.ItemSearchCond.class))).thenReturn(items);

        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(1, 10, "id", "desc", null);

        // when
        List<Item> result = sut.findItemsBySearchCond(searchCond);

        // then
        assertThat(result).hasSize(2)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(
                        tuple(id1, name1, price1),
                        tuple(id2, name2, price2)
                );
        verify(itemRepository, times(1)).findAllBySearchCond(any(ItemCommand.ItemSearchCond.class));
    }

    @DisplayName("상품 전체 개수가 현재 페이지보다 크면 쿼리로 전체 개수를 조회한다.")
    @Test
    void countItemsWhenContentSizeIsLessThanTotalSize() {
        // given
        long totalSize = 20;
        int contentSize = 10;
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(1, contentSize, "id", "desc", null);

        when(itemRepository.countAllBySearchCond(searchCond)).thenReturn(totalSize);

        // when
        long result = sut.countItemsBySearchCond(searchCond, contentSize);

        // then
        assertThat(result).isEqualTo(totalSize);
        verify(itemRepository, times(1)).countAllBySearchCond(searchCond);
    }

    @DisplayName("상품 전체 개수가 현재 페이지보다 작으면 쿼리 없이 전체 개수를 계산하여 반환한다.")
    @Test
    void countItemsWhenContentSizeIsGreaterThanTotalSize() {
        // given
        int size = 10;
        long totalCount = 5;
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(1, size, "id", "desc", null);

        // when
        long result = sut.countItemsBySearchCond(searchCond, (int) totalCount);

        // then
        assertThat(result).isEqualTo(totalCount);
        verifyNoInteractions(itemRepository);
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

        // when
        // then
        assertThatThrownBy(() -> sut.findItems(itemIds))
                .isInstanceOf(NoSuchItemException.class)
                .hasMessage(new NoSuchItemException().getMessage());
        verify(itemRepository, times(1)).findAllById(itemIds);
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

        // when
        // then
        assertThatThrownBy(() -> sut.getItem(id))
                .isInstanceOf(NoSuchItemException.class)
                .hasMessage(new NoSuchItemException().getMessage());
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

        // when
        // then
        assertThatThrownBy(() -> sut.getStocks(itemIds))
                .isInstanceOf(NoSuchItemStockException.class)
                .hasMessage(new NoSuchItemStockException().getMessage());
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

        // when
        // then
        assertThatThrownBy(() -> sut.getItemStockByItemId(id))
                .isInstanceOf(NoSuchItemStockException.class)
                .hasMessage(new NoSuchItemStockException().getMessage());
        verify(itemStockRepository, times(1)).findByItemId(id);
    }

    @DisplayName("상품의 재고수량을 차감할 수 있다.")
    @Test
    void deductStock() {
        // given
        Long itemStockId = 1L;
        int amount = 10;

        ItemStock itemStock = buildItemStock(itemStockId, buildItem(1L, "Test Item", 1000), 20);
        when(itemStockRepository.findByIdWithLock(itemStockId))
                .thenReturn(Optional.of(itemStock));

        // when
        sut.deductStock(itemStockId, amount);

        // then
        assertThat(itemStock.getAmount()).isEqualTo(10);
        verify(itemStockRepository, times(1)).findByIdWithLock(itemStockId);
    }

    @DisplayName("존재하지 않는 상품의 재고수량을 차감할 수 없다.")
    @Test
    void throwNoSuchItemStockExceptionWhenDeductStock() {
        // given
        Long itemStockId = 1L;
        int amount = 10;

        when(itemStockRepository.findByIdWithLock(itemStockId))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> sut.deductStock(itemStockId, amount))
                .isInstanceOf(NoSuchItemStockException.class)
                .hasMessage(new NoSuchItemStockException().getMessage());
        verify(itemStockRepository, times(1)).findByIdWithLock(itemStockId);
    }

    @DisplayName("상품의 재고수량을 복원할 수 있다.")
    @Test
    void restoreStock() {
        // given
        Long itemStockId = 1L;
        int amount = 10;

        ItemStock itemStock = buildItemStock(itemStockId, buildItem(1L, "Test Item", 1000), 20);
        when(itemStockRepository.findByIdWithLock(itemStockId))
                .thenReturn(Optional.of(itemStock));

        // when
        sut.restoreStock(itemStockId, amount);

        // then
        assertThat(itemStock.getAmount()).isEqualTo(30);
        verify(itemStockRepository, times(1)).findByIdWithLock(itemStockId);
    }

    @DisplayName("존재하지 않는 상품의 재고수량을 복원할 수 없다.")
    @Test
    void throwNoSuchItemStockExceptionWhenRestoreStock() {
        // given
        Long itemStockId = 1L;
        int amount = 10;

        when(itemStockRepository.findByIdWithLock(itemStockId))
                .thenReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> sut.restoreStock(itemStockId, amount))
                .isInstanceOf(NoSuchItemStockException.class)
                .hasMessage(new NoSuchItemStockException().getMessage());
        verify(itemStockRepository, times(1)).findByIdWithLock(itemStockId);
    }

    @DisplayName("상품들을 전달받은 순서대로 정렬하여 반환할 수 있다.")
    @Test
    void findItemsInSameOrder() {
        // given
        Long id1 = 1L;
        String name1 = "Test Item1";
        int price1 = 1000;
        Long id2 = 2L;
        String name2 = "Test Item2";
        int price2 = 1000;

        List<Long> topItemIds = List.of(id2, id1);
        List<Item> items = List.of(
                buildItem(id1, name1, price1),
                buildItem(id2, name2, price2)
        );

        when(itemRepository.findAllById(any())).thenReturn(items);

        // when
        List<Item> result = sut.findItemsInSameOrder(topItemIds);

        // then
        assertThat(result).hasSize(2)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(
                        tuple(id2, name2, price2),
                        tuple(id1, name1, price1)
                );
    }

    @DisplayName("상품들을 전달받은 순서대로 정렬하여 반환할 때 조회되지 않은 상품이 포함되더라도 예외가 발생하지 않고 순서대로 반환한다.")
    @Test
    void findItemsInSameOrderWhenNotExistItems() {
        // given
        Long id1 = 1L;
        String name1 = "Test Item1";
        int price1 = 1000;
        Long id2 = 2L;
        String name2 = "Test Item2";
        int price2 = 1000;
        Long notExistId = 3L;

        List<Long> topItemIds = List.of(notExistId, id2, id1);
        List<Item> items = List.of(
                buildItem(id1, name1, price1),
                buildItem(id2, name2, price2)
        );

        when(itemRepository.findAllById(any())).thenReturn(items);

        // when
        List<Item> result = sut.findItemsInSameOrder(topItemIds);

        // then
        assertThat(result).hasSize(2)
                .extracting(i -> tuple(i.getId(), i.getName(), i.getPrice()))
                .containsExactly(
                        tuple(id2, name2, price2),
                        tuple(id1, name1, price1)
                );
    }

    private Item buildItem(Long id, String name, int price) {
        return Item.builder().id(id).name(name).price(price).build();
    }

    private ItemStock buildItemStock(Long id, Item item, int amount) {
        return ItemStock.builder().id(id).amount(amount).item(item).build();
    }
}
