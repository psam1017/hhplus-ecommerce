package hhplus.ecommerce.server.domain.order.exception;

import hhplus.ecommerce.server.interfaces.common.api.ApiException;

public class NoSuchOrderException extends ApiException {
    public NoSuchOrderException() {
        super("존재하지 않는 주문입니다.");
    }
}
