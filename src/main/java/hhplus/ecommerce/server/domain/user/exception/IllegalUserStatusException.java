package hhplus.ecommerce.server.domain.user.exception;

import hhplus.ecommerce.server.domain.user.enumeration.UserStatus;
import hhplus.ecommerce.server.interfaces.web.support.model.api.ApiException;
import hhplus.ecommerce.server.interfaces.web.support.model.api.ApiResponse;
import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

import static hhplus.ecommerce.server.interfaces.web.support.model.api.ApiStatus.ILLEGAL_STATUS;

public class IllegalUserStatusException extends ApiException {
    @Override
    public ApiResponse<?> initialize() {
        return ApiResponse.of(
                ILLEGAL_STATUS,
                UserStatus.class.getSimpleName(),
                "현재 사용자 상태로는 요청을 수행할 수 없습니다.",
                null
        );
    }

    public IllegalUserStatusException(Documentable documentable) {
        this.response = illegalStatus(documentable);
    }

    public IllegalUserStatusException() {
        super();
    }
}
