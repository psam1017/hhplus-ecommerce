package hhplus.ecommerce.server.domain.user.exception;

import hhplus.ecommerce.server.interfaces.common.api.ApiException;

public class NoSuchUserException extends ApiException {
    public NoSuchUserException() {
        super("존재하지 않는 사용자입니다.");
    }
}
