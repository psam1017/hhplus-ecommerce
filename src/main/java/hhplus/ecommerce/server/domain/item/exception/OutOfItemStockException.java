package hhplus.ecommerce.server.domain.item.exception;

import hhplus.ecommerce.server.interfaces.exception.ApiException;

public class OutOfItemStockException extends ApiException {
    public OutOfItemStockException(int leftAmount) {
        super("재고가 부족합니다. 남은 재고는 %d개 입니다.".formatted(leftAmount));
    }
}
