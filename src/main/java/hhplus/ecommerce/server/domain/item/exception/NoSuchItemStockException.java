package hhplus.ecommerce.server.domain.item.exception;

import hhplus.ecommerce.server.interfaces.exception.ApiException;

public class NoSuchItemStockException extends ApiException {
    public NoSuchItemStockException() {
        super("존재하지 않는 재고정보입니다.");
    }
}
