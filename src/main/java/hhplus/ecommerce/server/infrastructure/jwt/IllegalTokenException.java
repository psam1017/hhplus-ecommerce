package hhplus.ecommerce.server.infrastructure.jwt;

import hhplus.ecommerce.server.interfaces.common.api.ApiException;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

import static hhplus.ecommerce.server.interfaces.common.api.ApiStatus.ILLEGAL_STATUS;

public class IllegalTokenException extends ApiException {
    @Override
    public ApiResponse<?> initialize() {
        return ApiResponse.of(
                ILLEGAL_STATUS,
                JwtStatus.class.getSimpleName(),
                "토큰 상태가 올바르지 않습니다. 다시 로그인해주세요.",
                null
        );
    }

    public IllegalTokenException(Documentable documentable) {
        this.response = illegalStatus(documentable);
    }

    public IllegalTokenException() {
        super();
    }
}
