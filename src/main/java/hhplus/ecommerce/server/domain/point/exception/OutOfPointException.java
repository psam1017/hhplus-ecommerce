package hhplus.ecommerce.server.domain.point.exception;

import hhplus.ecommerce.server.interfaces.common.api.ApiException;

public class OutOfPointException extends ApiException {
    public OutOfPointException(int leftAmount) {
        super("포인트가 부족합니다. 남은 포인트는 %dP 입니다.".formatted(leftAmount));
    }
}
