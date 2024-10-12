package hhplus.ecommerce.server.interfaces.common.api;

public abstract class ApiException extends RuntimeException {

    protected ApiResponse<?> response;

    public ApiException() {
        this.response = initialize();
    }

    public abstract ApiResponse<?> initialize();

    public ApiResponse<?> getResponse() {
        return this.response;
    }
}
