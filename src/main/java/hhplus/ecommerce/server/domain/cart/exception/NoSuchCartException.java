package hhplus.ecommerce.server.domain.cart.exception;

import hhplus.ecommerce.server.interfaces.common.api.ApiException;

public class NoSuchCartException extends ApiException {
    public NoSuchCartException() {
        super("존재하지 않는 장바구니입니다.");
    }
}
