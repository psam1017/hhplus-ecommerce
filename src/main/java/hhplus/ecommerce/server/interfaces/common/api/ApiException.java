package hhplus.ecommerce.server.interfaces.common.api;

public abstract class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }
}
