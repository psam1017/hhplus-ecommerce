package hhplus.ecommerce.server.domain.item.service;

public class ItemCommand {

    public record ItemSearchCond(
            Integer page,
            Integer size,
            String prop,
            String dir,
            String keyword
    ) {
        public static ItemSearchCond of(Integer page, Integer size, String prop, String dir, String keyword) {
            page = page == null || page < 1 ? 1 : page;
            size = size == null || size < 10 ? 10 : Math.min(size, 100);
            prop = prop == null ? "id" : prop;
            dir = dir == null ? "desc" : dir;
            keyword = keyword == null || keyword.isBlank() ? null : keyword;
            return new ItemSearchCond(page, size, prop, dir, keyword);
        }

        public long getLimit() {
            return size;
        }

        public long getOffset() {
            return (long) (page - 1) * size;
        }
    }
}
