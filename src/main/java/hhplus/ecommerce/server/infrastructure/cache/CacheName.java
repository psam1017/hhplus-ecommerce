package hhplus.ecommerce.server.infrastructure.cache;

public final class CacheName {

    public static final String REDISSON_LOCK_PREFIX = "lock:";
    public static final String ITEMS_PAGE = "cache:items:page";
    public static final String ITEMS_TOP = "cache:items:top";
    public static final String ITEM_PAGE_WARM = "cache:item:warm:page";
    public static final String ITEM_TOP_WARM = "cache:item:warm:top";
}
