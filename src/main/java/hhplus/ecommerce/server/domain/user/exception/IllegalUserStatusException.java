package hhplus.ecommerce.server.domain.user.exception;

import hhplus.ecommerce.server.interfaces.web.common.model.api.ApiException;
import hhplus.ecommerce.server.interfaces.web.common.model.api.ApiResponse;
import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

public class IllegalUserStatusException extends ApiException {
    @Override
    public ApiResponse<?> initialize() {
        return null;
    }

    public IllegalUserStatusException(Documentable documentable) {
        this.response = illegalStatus(documentable);
    }
}
