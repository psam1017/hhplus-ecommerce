package hhplus.ecommerce.server.domain.item.exception;

import hhplus.ecommerce.server.interfaces.exception.ApiException;

public class NoSuchItemException extends ApiException {
    public NoSuchItemException() {
        super("존재하지 않는 상품입니다.");
    }
}
