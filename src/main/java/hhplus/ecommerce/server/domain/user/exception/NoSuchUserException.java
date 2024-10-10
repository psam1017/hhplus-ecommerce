package hhplus.ecommerce.server.domain.user.exception;

import hhplus.ecommerce.server.interfaces.common.api.ApiException;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;

public class NoSuchUserException extends ApiException {
    @Override
    public ApiResponse<?> initialize() {
        return noSuchElement("조건에 해당하는 사용자를 찾을 수 없습니다.");
    }

    public NoSuchUserException(Long id) {
        this.response = noSuchElement("id " + id + "번에 해당하는 사용자를 찾을 수 없습니다.");
    }

    public NoSuchUserException() {
        super();
    }
}
