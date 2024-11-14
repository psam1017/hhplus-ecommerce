package hhplus.ecommerce.server.infrastructure.event;

import org.springframework.context.ApplicationEvent;

public class ItemHiddenEvent extends ApplicationEvent {

    private final Long itemId;

    public ItemHiddenEvent(Object source, Long itemId) {
        super(source);
        this.itemId = itemId;
    }

    public Long getItemId() {
        return itemId;
    }
}
