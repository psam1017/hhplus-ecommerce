package hhplus.ecommerce.server.interfaces.item.model.response;

import hhplus.ecommerce.server.application.common.model.pagination.PageInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ItemPageResult {

    private PageInfo pageInfo;
    private List<ItemSummary> items;

    @Builder
    protected ItemPageResult(PageInfo pageInfo, List<ItemSummary> items) {
        this.pageInfo = pageInfo;
        this.items = items;
    }
}
