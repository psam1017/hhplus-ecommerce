package hhplus.ecommerce.server.interfaces.web.order.model.response;

import hhplus.ecommerce.server.domain.order.enumeration.OrderStatus;
import hhplus.ecommerce.server.interfaces.web.support.jsonformat.KoreanDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderDetail {

    private Long id;
    private String title;
    @KoreanDateTime
    private LocalDateTime orderDateTime;
    private OrderStatus orderStatus;
    private String orderStatusValue;
    private List<OrderDetailItem> orderItems;

    @Builder
    protected OrderDetail(Long id, String title, LocalDateTime orderDateTime, OrderStatus orderStatus, String orderStatusValue, List<OrderDetailItem> orderItems) {
        this.id = id;
        this.title = title;
        this.orderDateTime = orderDateTime;
        this.orderStatus = orderStatus;
        this.orderStatusValue = orderStatusValue;
        this.orderItems = orderItems;
    }

    @Getter
    @NoArgsConstructor
    public static class OrderDetailItem {

        private Long orderItemId;
        private Long itemId;
        private String name;
        private Integer price;
        private Integer amount;

        @Builder
        protected OrderDetailItem(Long orderItemId, Long itemId, String name, Integer price, Integer amount) {
            this.orderItemId = orderItemId;
            this.itemId = itemId;
            this.name = name;
            this.price = price;
            this.amount = amount;
        }
    }
}
