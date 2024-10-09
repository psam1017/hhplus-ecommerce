package hhplus.ecommerce.server.interfaces.web.support.model.api;

import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

import static hhplus.ecommerce.server.interfaces.web.support.model.api.ApiStatus.*;

/**
 * Create a business exception by inheriting ApiException so that the controller advice can catch the exception.
 * By implementing abstract method named 'initialize', the constructor will automatically specify that ApiResponse.
 */
public abstract class ApiException extends RuntimeException {

    protected ApiResponse<?> response;

    public ApiException() {
        this.response = initialize();
    }

    public abstract ApiResponse<?> initialize();

    public ApiResponse<?> getResponse() {
        return this.response;
    }

    public ApiResponse<?> duplicateKey(String debug) {
        return ApiResponse.error(DUPLICATE_KEY, this.getClass(), debug, null);
    }

    public ApiResponse<?> noSuchElement(String debug) {
        return ApiResponse.error(NO_SUCH_ELEMENT, this.getClass(), debug, null);
    }

    public ApiResponse<?> accessDenial(String debug) {
        return ApiResponse.error(ACCESS_DENIAL, this.getClass(), debug, null);
    }

    public ApiResponse<?> illegalData(String debug) {
        return ApiResponse.error(ILLEGAL_DATA, this.getClass(), debug, null);
    }

    public ApiResponse<?> illegalStatus(Documentable documentable) {
        return ApiResponse.of(
                ILLEGAL_STATUS,
                documentable.getClass().getSimpleName() + documentable.key(),
                documentable.value(),
                null
        );
    }

    public ApiResponse<?> illegalRole(Documentable documentable) {
        return ApiResponse.of(
                ILLEGAL_ROLE,
                documentable.getClass().getSimpleName() + documentable.key(),
                documentable.value(),
                null
        );
    }
}
