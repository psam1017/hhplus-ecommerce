package hhplus.ecommerce.server.unit.domain.item;

import hhplus.ecommerce.server.domain.item.service.ItemCommand;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ItemCommandTest {

    @DisplayName("검색조건에서 값을 입력하지 않으면, page, size, prop, dir 의 기본값이 설정된다.")
    @Test
    void itemSearchCondDefaultValues() {
        // given
        // when
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(null, null, null, null, null);

        // then
        assertThat(searchCond.page()).isEqualTo(1);
        assertThat(searchCond.size()).isEqualTo(10);
        assertThat(searchCond.prop()).isEqualTo("id");
        assertThat(searchCond.dir()).isEqualTo("desc");
        assertThat(searchCond.keyword()).isNull();
    }

    @DisplayName("검색조건에서 page 는 최소 1 로 설정된다.")
    @Test
    void itemSearchCondPageMinValue() {
        // given
        // when
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(0, null, null, null, null);

        // then
        assertThat(searchCond.page()).isEqualTo(1);
    }

    @DisplayName("검색조건에서 size 는 최소 10으로 설정된다.")
    @Test
    void itemSearchCondSizeMinMaxValue() {
        // given
        // when
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(null, 9, null, null, null);

        // then
        assertThat(searchCond.size()).isEqualTo(10);
    }

    @DisplayName("검색조건에서 size 는 최대 100으로 설정된다.")
    @Test
    void itemSearchCondSizeMaxValue() {
        // given
        // when
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(null, 101, null, null, null);

        // then
        assertThat(searchCond.size()).isEqualTo(100);
    }

    @DisplayName("검색어 keyword 가 빈 문자열인 경우 null 로 설정된다.")
    @Test
    void itemSearchCondKeywordEmptyString() {
        // given
        // when
        ItemCommand.ItemSearchCond searchCond = ItemCommand.ItemSearchCond.of(null, null, null, null, "   ");

        // then
        assertThat(searchCond.keyword()).isNull();
    }
}
