package hhplus.ecommerce.server.global.web.api;

import hhplus.ecommerce.server.domain.common.enumeration.Documentable;

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
        return ApiResponse.error(ApiStatus.DUPLICATE_KEY, this.getClass(), debug, null);
    }

    public ApiResponse<?> noSuchElement(String debug) {
        return ApiResponse.error(ApiStatus.NO_SUCH_ELEMENT, this.getClass(), debug, null);
    }

    public ApiResponse<?> accessDenial(String debug) {
        return ApiResponse.error(ApiStatus.ACCESS_DENIAL, this.getClass(), debug, null);
    }

    public ApiResponse<?> illegalData(String debug) {
        return ApiResponse.error(ApiStatus.ILLEGAL_DATA, this.getClass(), debug, null);
    }

    public ApiResponse<?> illegalStatus(Documentable documentable) {
        return ApiResponse.of(
                ApiStatus.ILLEGAL_STATUS,
                documentable.getClass().getSimpleName() + documentable.key(),
                documentable.value(),
                null
        );
    }

    public ApiResponse<?> illegalRole(Documentable documentable) {
        return ApiResponse.of(
                ApiStatus.ILLEGAL_ROLE,
                documentable.getClass().getSimpleName() + documentable.key(),
                documentable.value(),
                null
        );
    }
}
