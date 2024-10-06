package hhplus.ecommerce.server.infrastructure.jwt;

import hhplus.ecommerce.server.interfaces.web.common.model.api.ApiException;
import hhplus.ecommerce.server.interfaces.web.common.model.api.ApiResponse;
import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

public class IllegalTokenException extends ApiException {
    @Override
    public ApiResponse<?> initialize() {
        return null;
    }

    public IllegalTokenException(Documentable documentable) {
        this.response = illegalStatus(documentable);
    }
}
