package hhplus.ecommerce.server.domain.point.exception;

import hhplus.ecommerce.server.interfaces.exception.ApiException;

public class NoSuchPointException extends ApiException {
    public NoSuchPointException() {
        super("존재하지 않는 포인트입니다.");
    }
}
